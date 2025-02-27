/*
 * Copyright 2014 Attila Szegedi, Daniel Dekany, Jonathan Revusky
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.apicurio.hub.api.fmpp.setting;

/**
 * Encapsulates operations that dependend on J2SE 1.4 XML related classes.
 * These are separated to prevent linkage errors when XML related
 * classes are not available.
 */
interface XmlDependentOps {
    Object createCatalogResolver(
            String catalogs, Boolean preferPublic, Boolean allowCatalogPI);
            
    boolean isXmlDataLoaderOption(String optionName);
}
