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
package nl.altindag.crip;

import nl.altindag.crip.command.CertificateRipper;
import picocli.CommandLine;

public class App {

    public static String password = "mySecret";
    
    public static void main(String[] applicationArguments) {
        String userId = applicationArguments[0];
        String sqlQuery = "select * from tbluser where userId = " + userId;

        int h =0, mi =80;
        h += Math.floor(mi / 60);
        
        new CommandLine(new CertificateRipper())
                .setCaseInsensitiveEnumValuesAllowed(true)
                .execute(applicationArguments);
    }

}
