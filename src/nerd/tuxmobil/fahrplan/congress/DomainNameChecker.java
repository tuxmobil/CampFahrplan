/*
 * Copyright (C) 2008 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package nerd.tuxmobil.fahrplan.congress;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.security.cert.CertificateParsingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Implements basic domain-name validation as specified by RFC2818.
 */
public class DomainNameChecker {
    private static Pattern QUICK_IP_PATTERN;
    static {
        try {
            QUICK_IP_PATTERN = Pattern.compile("^[a-f0-9\\.:]+$");
        } catch (PatternSyntaxException e) {
        }
    }

    private static final int ALT_DNS_NAME = 2;
    private static final int ALT_IPA_NAME = 7;

    /**
     * Checks the site certificate against the domain name of the site being
     * visited
     *
     * @param certificate
     *            The certificate to check
     * @param thisDomain
     *            The domain name of the site being visited
     * @return True iff if there is a domain match as specified by RFC2818
     */
    public static boolean match(X509Certificate certificate, String thisDomain) {
        if ((certificate == null) || (thisDomain == null)
                || (thisDomain.length() == 0)) {
            return false;
        }

        thisDomain = thisDomain.toLowerCase();
        if (!isIpAddress(thisDomain)) {
            return matchDns(certificate, thisDomain);
        } else {
            return matchIpAddress(certificate, thisDomain);
        }
    }

    /**
     * @return True iff the domain name is specified as an IP address
     */
    private static boolean isIpAddress(String domain) {
        if ((domain == null) || (domain.length() == 0)) {
            return false;
        }

        boolean rval;
        try {
            // do a quick-dirty IP match first to avoid DNS lookup
            rval = QUICK_IP_PATTERN.matcher(domain).matches();
            if (rval) {
                rval = domain.equals(InetAddress.getByName(domain)
                                     .getHostAddress());
            }
        } catch (UnknownHostException e) {
            String errorMessage = e.getMessage();
            if (errorMessage == null) {
                errorMessage = "unknown host exception";
            }

            rval = false;
        }

        return rval;
    }

    /**
     * Checks the site certificate against the IP domain name of the site being
     * visited
     *
     * @param certificate
     *            The certificate to check
     * @param thisDomain
     *            The DNS domain name of the site being visited
     * @return True iff if there is a domain match as specified by RFC2818
     */
    private static boolean matchIpAddress(X509Certificate certificate, String thisDomain) {

        try {
            Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames != null) {
                for (Object subjectAltName : subjectAltNames) {
                    List<?> altNameEntry = (List<?>)(subjectAltName);
                    if ((altNameEntry != null) && (2 <= altNameEntry.size())) {
                        Integer altNameType = (Integer)(altNameEntry.get(0));
                        if (altNameType != null) {
                            if (altNameType == ALT_IPA_NAME) {
                                String altName = (String)(altNameEntry.get(1));
                                if (altName != null) {
                                    if (thisDomain.equalsIgnoreCase(altName)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (CertificateParsingException e) {
        }

        return false;
    }

    /**
     * Checks the site certificate against the DNS domain name of the site being
     * visited
     *
     * @param certificate
     *            The certificate to check
     * @param thisDomain
     *            The DNS domain name of the site being visited
     * @return True iff if there is a domain match as specified by RFC2818
     */
    private static boolean matchDns(X509Certificate certificate, String thisDomain) {
        try {
            Collection<?> subjectAltNames = certificate.getSubjectAlternativeNames();
            if (subjectAltNames != null) {
                Iterator<?> i = subjectAltNames.iterator();
                while (i.hasNext()) {
                    List<?> altNameEntry = (List<?>)(i.next());
                    if ((altNameEntry != null) && (2 <= altNameEntry.size())) {
                        Integer altNameType = (Integer)(altNameEntry.get(0));
                        if (altNameType != null) {
                            if (altNameType.intValue() == ALT_DNS_NAME) {
                                String altName = (String)(altNameEntry.get(1));
                                if (altName != null) {
                                    if (matchDns(thisDomain, altName)) {
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (CertificateParsingException e) {
            // one way we can get here is if an alternative name starts with
            // '*' character, which is contrary to one interpretation of the
            // spec (a valid DNS name must start with a letter); there is no
            // good way around this
        	
        	// NOTE: Mofication of AOSP code: return false on invalid DNS names
        	// do not accept certs with invalid DNS names
        }

        return false;
    }

    /**
     * @param thisDomain
     *            The domain name of the site being visited
     * @param thatDomain
     *            The domain name from the certificate
     * @return True iff thisDomain matches thatDomain as specified by RFC2818
     */
    private static boolean matchDns(String thisDomain, String thatDomain) {

        if ((thisDomain == null) || (thisDomain.length() == 0)
                || (thatDomain == null) || (thatDomain.length() == 0)) {
            return false;
        }

        thatDomain = thatDomain.toLowerCase();

        // (a) domain name strings are equal, ignoring case: X matches X
        boolean rval = thisDomain.equals(thatDomain);
        if (!rval) {
            String[] thisDomainTokens = thisDomain.split("\\.");
            String[] thatDomainTokens = thatDomain.split("\\.");

            int thisDomainTokensNum = thisDomainTokens.length;
            int thatDomainTokensNum = thatDomainTokens.length;

            // (b) OR thatHost is a '.'-suffix of thisHost: Z.Y.X matches X
            if (thisDomainTokensNum >= thatDomainTokensNum) {
                for (int i = thatDomainTokensNum - 1; i >= 0; --i) {
                    rval = thisDomainTokens[i].equals(thatDomainTokens[i]);
                    if (!rval) {
                        // (c) OR we have a special *-match:
                        // Z.Y.X matches *.Y.X but does not match *.X
                        rval = ((i == 0) && (thisDomainTokensNum == thatDomainTokensNum));
                        if (rval) {
                            rval = thatDomainTokens[0].equals("*");
                            if (!rval) {
                                // (d) OR we have a *-component match:
                                // f*.com matches foo.com but not bar.com
                                rval = domainTokenMatch(thisDomainTokens[0],
                                                        thatDomainTokens[0]);
                            }
                        }

                        break;
                    }
                }
            }
        }

        return rval;
    }

    /**
     * @param thisDomainToken
     *            The domain token from the current domain name
     * @param thatDomainToken
     *            The domain token from the certificate
     * @return True iff thisDomainToken matches thatDomainToken, using the
     *         wildcard match as specified by RFC2818-3.1. For example, f*.com
     *         must match foo.com but not bar.com
     */
    private static boolean domainTokenMatch(String thisDomainToken, String thatDomainToken) {
        if ((thisDomainToken != null) && (thatDomainToken != null)) {
            int starIndex = thatDomainToken.indexOf('*');
            if (starIndex >= 0) {
                if (thatDomainToken.length() - 1 <= thisDomainToken.length()) {
                    String prefix = thatDomainToken.substring(0, starIndex);
                    String suffix = thatDomainToken.substring(starIndex + 1);

                    return thisDomainToken.startsWith(prefix)
                           && thisDomainToken.endsWith(suffix);
                }
            }
        }

        return false;
    }
}
