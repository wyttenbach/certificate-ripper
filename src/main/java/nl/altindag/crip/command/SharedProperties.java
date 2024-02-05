/*
 * Copyright 2021 Thunderberry.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.altindag.crip.command;

import nl.altindag.crip.model.CertificateHolder;
import nl.altindag.ssl.util.CertificateExtractingClient;
import nl.altindag.ssl.util.internal.UriUtils;
import picocli.CommandLine.Option;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.security.cert.X509Certificate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static nl.altindag.ssl.util.internal.StringUtils.isNotBlank;

@SuppressWarnings("unused")
public class SharedProperties {

    @Option(names = {"-u", "--url"}, description = "Url of the target server to extract the certificates", required = true)
    private String[] urls;
    private List<String> uniqueUrls;

    @Option(names = {"--proxy-host"}, description = "Proxy host")
    private String proxyHost;

    @Option(names = {"--proxy-port"}, description = "Proxy port")
    private Integer proxyPort;

    @Option(names = {"--proxy-user"}, description = "User for authenticating the user for the given proxy")
    private String proxyUser;

    @Option(names = {"--proxy-password"}, interactive = true, description = "Password for authenticating the user for the given proxy")
    private String proxyPassword;

    @Option(names = {"-t", "--timeout"}, description = "Amount of milliseconds till the ripping should timeout")
    private Integer timeoutInMilliseconds;

    public CertificateHolder getCertificateHolder() {
        List<String> uniqueUrls = getUrls();

        CertificateExtractingClient client = createClient();

        Map<String, List<X509Certificate>> urlsToCertificates = uniqueUrls.stream()
                .distinct()
                .map(url -> new AbstractMap.SimpleEntry<>(url, client.get(url)))
                .collect(Collectors.collectingAndThen(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (key1, key2) -> key1, LinkedHashMap::new), Collections::unmodifiableMap));

        return new CertificateHolder(urlsToCertificates);
    }

    public List<X509Certificate> getCertificatesFromFirstUrl() {
        CertificateExtractingClient client = createClient();
        return client.get(urls[0]);
    }

    private CertificateExtractingClient createClient() {
        CertificateExtractingClient.Builder clientBuilder = CertificateExtractingClient.builder();

        if (isNotBlank(proxyHost) && proxyPort != null) {
            clientBuilder.withProxy(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort)));
        }
        if (isNotBlank(proxyUser) && isNotBlank(proxyPassword)) {
            clientBuilder.withPasswordAuthentication(new PasswordAuthentication(proxyUser, proxyPassword.toCharArray()));
        }

        if (timeoutInMilliseconds != null && timeoutInMilliseconds > 0) {
            clientBuilder.withTimeout(timeoutInMilliseconds);
        }

        return clientBuilder.build();
    }

    public List<String> getUrls() {
        if (uniqueUrls != null) {
            return uniqueUrls;
        }

        uniqueUrls = new ArrayList<>();
        Map<String, List<Integer>> hostToPort = new HashMap<>();

        for (String url : urls) {
            String host = UriUtils.extractHost(url);
            int port = UriUtils.extractPort(url);

            if (hostToPort.containsKey(host)) {
                List<Integer> ports = hostToPort.get(host);
                if (ports.contains(port)) {
                    continue;
                }
            }

            List<Integer> ports = hostToPort.getOrDefault(host, new ArrayList<>());
            ports.add(port);

            hostToPort.put(host, ports);
            uniqueUrls.add(url);
        }
        return uniqueUrls;
    }

}
