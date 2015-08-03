/*
 * Copyright 2005-2015 WSO2, Inc. (http://wso2.com)
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
var render = function (theme, data, meta, require) {

    if(data.error.length == 0 ){
        theme('login', {
            page_meta: [
                {
                    partial:'index_title',
                    context:{
                        page_title:'WSO2 Private PaaS Login',
                        page_description:'WSO2 Private PaaS Login'
                    }
                }
            ],
            header:[
                {
                    partial: 'login_header',
                    context:{
                    }
                }
            ],
            content: [

                {
                    partial: 'login_body',
                    context:{
                        content_title:''

                    }
                }
            ]
        });

    }else{

        theme('login', {
            page_meta: [
                {
                    partial:'index_title',
                    context:{
                        page_title:'WSO2 Private PaaS Home - Error',
                        page_description:'WSO2 Private PaaS Home - Error'
                    }
                }
            ],
            header:[
                {
                    partial: 'login_header',
                    context:{
                    }
                }
            ],
            content: [

                {
                    partial: 'error_page',
                    context:{
                        error:data.error,
                        content_title:'Sorry Something went Wrong...! ',
                        content_body:{

                        }

                    }
                }
            ]
        });
    }
};