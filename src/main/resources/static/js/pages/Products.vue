<template>
    <b-container fluid style="width: 100%;">
        <v-progress-linear indeterminate color="#e52d00" v-if="loading"></v-progress-linear>

        <v-toolbar flat v-if="!loading">
            <v-toolbar-items style="padding-left: 8%">
                <v-btn v-if="showFiltersButtonToolbar" small depressed outlined color="black" max-height="50%" style="margin-top: 15px" @click="returnFilters()">Открыть фильтры</v-btn>

                <router-link to="/catalog">
                    <v-btn depressed text small height="100%">
                        Каталог
                    </v-btn>
                </router-link>

                <router-link :to="'/catalog/'+linkCategory" >
                    <v-btn depressed text small height="100%">
                        {{linkCategory}}
                    </v-btn>
                </router-link>

                <v-btn depressed disabled text small>{{linkProductGroup}}</v-btn>
            </v-toolbar-items>
        </v-toolbar>

        <v-navigation-drawer app width="375" v-if="showFilters" :clipped="$vuetify.breakpoint.mdAndUp" style="margin-left: 70px">
            <template v-slot:prepend>
                <v-container>
                    <v-row>
                        <v-col>
                            <span><strong>{{ (group.charAt(0).toUpperCase() + group.substr(1)).replace('_',' ') }}</strong></span>
                        </v-col>
                        <v-col cols="3">
                            <v-btn icon @click="hideFilters()">
                                <v-icon>mdi-chevron-left</v-icon>
                            </v-btn>
                        </v-col>
                    </v-row>
                    <v-row>
                        <v-col>
                            <p v-if="!loading" style="text-align: left; margin-top: -20px; margin-bottom: -20px;" v-model="totalProductsFound">Всего товаров: <strong>{{totalProductsFound}}</strong></p>
                        </v-col>
                    </v-row>
                </v-container>
            </template>

            <v-divider></v-divider>

            <v-card-actions style="padding-right: -15px;">

                <div v-if="loading">
                    <v-progress-circular style="margin-left: 150%"
                                         :size="75"
                                         :width="5"
                                         color="#e52d00"
                                         indeterminate
                    ></v-progress-circular>
                </div>

                <v-expansion-panels v-else
                                    multiple
                                    focusable

                >
                    <!--Фильтры-цены input-->
                    <v-expansion-panel>
                        <v-expansion-panel-header ripple>Цены</v-expansion-panel-header>
                        <v-expansion-panel-content>
                            <v-card-actions>
                                <v-range-slider class="align-bottom" v-model="priceRange" :min="min" :max="max" hide-details color="#e52d00" @end="filterProducts('price;' + priceRange)">
                                    <template v-slot:prepend>
                                        <v-text-field @input="filterProducts('price;' + priceRange[0] + ',' + priceRange[1])" color="#e52d00" v-model="priceRange[0]" type="string" style="width: 60px" class="mt-0 pt-0"></v-text-field>
                                    </template>
                                    <template v-slot:append>
                                        <v-text-field @input="filterProducts('price;' + priceRange[0] + ',' + priceRange[1])" color="#e52d00" v-model="priceRange[1]" type="string" style="width: 60px" class="mt-0 pt-0"></v-text-field>
                                    </template>
                                </v-range-slider>
                            </v-card-actions>
                        </v-expansion-panel-content>
                    </v-expansion-panel>

                    <!--Фильтры-бренды checkbox-->
                    <v-expansion-panel ripple >
                        <v-expansion-panel-header ripple>Бренды</v-expansion-panel-header>
                        <v-expansion-panel-content eager style="margin-top: 10px">
                            <div v-for="brand in twoColsBrands">
                                <v-row>
                                    <v-col class="p-0">
                                        <v-checkbox color="#e52d00" class="mt-1" @change="filterProducts('brand;' + brand.firstBrand)" v-model="selectedBrands" :label="brand.firstBrand" :value="brand.firstBrand" height="2"></v-checkbox>
                                    </v-col>

                                    <v-col class="p-0 " v-if="brand.secondBrand !== undefined">
                                        <v-checkbox color="#e52d00" class="mt-1" @change="filterProducts('brand;' + brand.secondBrand)" v-model="selectedBrands" :label="brand.secondBrand" :value="brand.secondBrand" height="2"></v-checkbox>
                                    </v-col>
                                </v-row>
                            </div>
                        </v-expansion-panel-content>
                    </v-expansion-panel>


                    <!--Фильтры-параметры checkbox-->
                    <v-expansion-panel v-for="[key, val] of filtersParams" :key="key">
                        <v-expansion-panel-header ripple>{{ key.charAt(0).toUpperCase() + key.substr(1) }}</v-expansion-panel-header>
                        <v-expansion-panel-content eager style="margin-top: 10px">
                            <div v-for="(param, i) in val" :key="i" :brand="param">
                                <v-row>
                                    <v-col class="p-0 m-0">
                                        <v-checkbox :disabled="checkFilterInCheckList(key + ': ' + param)" color="#e52d00" class="mt-2" @change="filterProducts('param;' + key + ': ' + param)" v-model="selectedParams" :label="param" :value="key +': '+ param" height="2"></v-checkbox>
                                    </v-col>
                                </v-row>
                            </div>
                        </v-expansion-panel-content>
                    </v-expansion-panel>


                    <!--Фильтры-диапазоны input-->
                    <v-expansion-panel v-for="[key, val] of filtersDiapasons" :key="key" >
                        <v-expansion-panel-header  ripple v-if="val[1] !== undefined" :class="{'red': val[1] === undefined}">{{ key.charAt(0).toUpperCase() + key.substr(1) }}</v-expansion-panel-header>
                        <v-expansion-panel-content eager>
                            <v-row>
                                <v-col>
                                    <v-range-slider color="#e52d00" v-model="val" :min="val[0]" :max="val[1]" hide-details class="align-center" @end="filterProducts('diapason;' + key +':'+ val)">
                                        <template v-slot:prepend>
                                            <v-text-field color="#e52d00" @input="filterProducts('diapason;' + key +':'+ diapasonValues.get(key))" v-model="diapasonValues.get(key)[0]" class="mt-0 pt-0"  type="string" style="width: 60px"></v-text-field>
                                        </template>
                                        <template v-slot:append>
                                            <v-text-field color="#e52d00" @input="filterProducts('diapason;' + key +':'+ diapasonValues.get(key))" v-model="diapasonValues.get(key)[1]" class="mt-0 pt-0"  type="string" style="width: 60px"></v-text-field>
                                        </template>
                                    </v-range-slider>
                                </v-col>
                            </v-row>
                        </v-expansion-panel-content>
                    </v-expansion-panel>
                </v-expansion-panels>
            </v-card-actions>
        </v-navigation-drawer>


        <b-container fluid fill-height style="padding-left: 4%">

            <!--Список фильтров-особенностей-->
            <v-slide-group multiple show-arrows class="mt-5" style="margin-left: 5%">
                <v-slide-item v-for="feature in filtersFeats" :key="feature" v-slot:default="{ active, toggle }">
                    <v-btn :disabled="checkFilterInCheckList(feature)" class="mx-2 btn-actv" style="background-color: #fafafa" :input-value="active" active-class="orange text" depressed rounded @click="toggle" @mouseup="filterProducts('feature;' + feature)">
                        {{ feature }}
                    </v-btn>
                </v-slide-item>
            </v-slide-group>

            <v-row style="margin-left: 5%">
                <product-card v-for="product in products" :key="product.productID" :product="product" :products="products"></product-card>
            </v-row>

            <v-row v-if="loadingMoreProducts">
                <v-col>
                    <v-progress-circular
                            :size="50"
                            color="#e52d00"
                            indeterminate
                            style="margin-left: 50%; margin-top: 1%"
                    ></v-progress-circular>
                </v-col>
            </v-row>
        </b-container>
    </b-container>
</template>


<script>
    /*!!! Если modelName isEmpty, to тогда выводить singleType + originalName !!!*/
    import axios from 'axios'
    import ProductCard from "components/products/ProductCard.vue";
    export default {
        components: {ProductCard},
        data() {
            return {
                showFilters: true,
                loading: true,
                products: [],
                selectedBrands: [],
                selectedParams: [],
                selectedDiapasons: {},
                selectedFeatures: [],
                filtersPrice: [],
                filtersBrands: [],
                filtersFeats: [],
                filtersDiapasons: new Map(),
                diapasonValues: new Map(),
                filtersParams: new Map(),
                drawer: true,
                mini: false,
                group: decodeURI(window.location.href).substr(decodeURI(window.location.href).lastIndexOf('/') + 1),
                min: '',
                max: '',
                priceRange: [],
                selected: [],
                page: 1,
                totalPages: 0,
                linkCategory: '',
                linkProductGroup: '',
                requestGroup:'',
                productsRequest: '',
                filtersRequest: '',
                pageRequest: '',
                totalProductsFound: 0,
                scrollPage: 1,
                filtersScrollPage: 0,
                showFiltersButtonToolbar: false,
                loadingMoreProducts: false,
                filtersChecklist: [],
                filtersQueue: []
            }
        },
        created() {
            this.requestGroup    = (decodeURI(window.location.href).substr(decodeURI(window.location.href).lastIndexOf('/'))).replace('_', ' ')
            this.pageRequest     = '/api/products/group' + this.requestGroup
            this.productsRequest = '/api/products/group' + this.requestGroup + '/0'
            this.filtersRequest  = '/api/products/build_filters' + this.requestGroup

            let linkGroup = this.requestGroup.replace('/', '')
            linkGroup = linkGroup.charAt(0).toUpperCase() + linkGroup.substr(1)
            this.linkProductGroup = linkGroup

            /*ЗАГРУЗИТЬ СПИСОК ФИЛЬТРОВ*/
            axios.get(this.filtersRequest).then(response => {

                console.log(response.data)

                this.filtersChecklist = response.data.checklist
                console.log(this.filtersChecklist)

                let prices = response.data.prices
                this.min = prices[0]
                this.max = prices[1]
                this.priceRange[0] = prices[0]
                this.priceRange[1] = prices[1]

                this.filtersBrands = response.data.brands
                this.filtersFeats  = response.data.features

                let diapasons = response.data.diapasons
                for (let [key, value] of Object.entries(diapasons)) {
                    this.filtersDiapasons.set(key, value.slice(", "))
                    this.diapasonValues.set(key, value.slice(", "))
                }

                let params = response.data.params
                for (const [key, value] of Object.entries(params)) this.filtersParams.set(key, value)
            });

            /*Load Products*/
            axios.get(this.productsRequest).then(response => {
                this.products = response.data.content
                this.totalPages = response.data.totalPages
                this.totalProductsFound = response.data.totalElements
                this.linkCategory = this.products[0].productCategory
                this.loading = false
            })
        },
        mounted() {

            /*ПОДРУЗИТЬ ТОВАРЫ ПРИ ПРОМОТКИ ДО НИЗА ЭКРАНА*/
            window.onscroll = () => {
                const el = document.documentElement
                const isBottomOfScreen = el.scrollTop + window.innerHeight === el.offsetHeight

                if(isBottomOfScreen) {
                    console.log('bottomLoad')

                    if (Object.keys(this.filtersQueue).length === 0) {
                        if (this.products.length >=15 ) {
                            this.loadingMoreProducts = true
                        }

                        axios.get('/api/products/group'+this.requestGroup + '/' + this.scrollPage).then(response => {
                            //console.log(response.data.content)
                            this.products = this.products.concat(response.data.content)
                            this.scrollPage+=1
                            this.loadingMoreProducts = false
                        })
                    }
                    else
                    {
                        if (this.products.length >=15 ) {
                            this.loadingMoreProducts = true

                            this.filtersScrollPage+=1
                            const filterURL = '/api/products/filter' + this.requestGroup + '/' + this.filtersScrollPage

                            axios.post(filterURL, this.filtersQueue).then(response => {
                                console.log(response.data[0].content)
                                this.products = this.products.concat(response.data[0].content)
                                this.loadingMoreProducts = false
                            })
                        }
                    }
                }
            }
        },
        beforeDestroy() {
            window.onscroll = null
            this.scrollPage = 1
        },
        computed: {
            twoColsBrands() {
                const twoColsBrands = []
                for (let i = 0; i < this.filtersBrands.length; i+=2) {
                    let first = this.filtersBrands[i]
                    let second = this.filtersBrands[i+1]
                    twoColsBrands.push({
                        firstBrand: first, secondBrand: second
                    })
                }
                return twoColsBrands
            },
            filterButton() {
                return this.$store.state.filtersClosedButton
            }
        },
        methods: {
            checkFilterInCheckList(value) {
                const val = value.toString().toLowerCase().trim()
                return !this.filtersChecklist.includes(val)
            },
            loadPage(page) {
                let pageRequest = this.pageRequest + '/' + page
                axios.get(pageRequest).then(response =>
                    this.products = response.data.content)
            },

            /*ФИЛЬТРАЦИЯ ТОВАРОВ*/
            filterProducts(param) {

                this.filtersScrollPage = 0

                if (param.includes('diapason;') || param.includes('price;')) {
                    let checkDiapason = param.substr(0, param.lastIndexOf(':'))

                    for (let filter in this.filtersQueue) {
                        if (this.filtersQueue[filter].includes(checkDiapason)) {
                            this.filtersQueue.splice(this.filtersQueue.indexOf(this.filtersQueue[filter]), 1);
                        }
                    }
                }

                if (this.filtersQueue.includes(param)) {
                    console.log('удаление из массива')
                    this.filtersQueue.splice(this.filtersQueue.indexOf(param), 1);
                }
                else this.filtersQueue.push(param)

                console.log(this.filtersQueue)

                const filterURL = '/api/products/filter' + this.requestGroup + '/' + this.filtersScrollPage
                axios.post(filterURL, this.filtersQueue).then(response => {
                    const page = response.data[0]
                    const filtersCheckList = response.data[1]

                    this.products = page.content
                    this.totalPages = page.totalPages
                    this.totalProductsFound = page.totalElements
                    this.filtersChecklist = filtersCheckList
                })
            },
            hideFilters() {
                this.showFilters = false
                this.showFiltersButtonToolbar = true
            },
            returnFilters() {
                this.showFilters = true
                this.showFiltersButtonToolbar = false
            }
        }
    }
</script>

