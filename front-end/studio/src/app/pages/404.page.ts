/**
 * @license
 * Copyright 2017 JBoss Inc
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

import {Component} from "@angular/core";
import {AbstractPageComponent} from "../components/page-base.component";
import {ActivatedRoute} from "@angular/router";
import {Title} from "@angular/platform-browser";

/**
 * The Not Found (404) Page component - shown when navigating to a route that does not exist.
 */
@Component({
    selector: "not-found-page",
    templateUrl: "404.page.html"
})
export class NotFoundPageComponent extends AbstractPageComponent {

    /**
     * C'tor.
     * @param route
     * @param titleService
     */
    constructor(route: ActivatedRoute, titleService: Title) {
        super(route, titleService);
    }

    /**
     * The page title.
     * 
     */
    protected pageTitle(): string {
        return "Boilerplate Studio - Not Found";
    }

    /**
     * @see AbstractPageComponent.loadAsyncPageData
     */
    public loadAsyncPageData(): void {
    }

}
