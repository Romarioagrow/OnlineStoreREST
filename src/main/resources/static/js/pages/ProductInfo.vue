<template>
    <div>
        <v-container class="pt-0">

            <v-row>
                <v-col class="pt-0">
                    <v-toolbar flat v-if="!loading">
                        <v-toolbar-items>

                            <router-link :to="'/catalog/'+product.productCategory">
                                <v-btn depressed text small height="100%">
                                    Каталог
                                </v-btn>
                            </router-link>

                            <router-link :to="'/catalog/'+product.productCategory" >
                                <v-btn depressed text small height="100%">
                                    {{product.productCategory}}
                                </v-btn>
                            </router-link>

                            <router-link :to="linkBack">
                                <v-btn depressed text small height="100%">
                                    {{product.productGroup}}
                                </v-btn>
                            </router-link>

                            <v-btn depressed text small disabled height="100%">
                                {{product.productGroup}} {{product.brand}}
                            </v-btn>


                            <v-btn depressed disabled text small>{{linkProductGroup}}</v-btn>
                        </v-toolbar-items>
                    </v-toolbar>
                </v-col>
            </v-row>

            <v-row>
                <v-col>
                    <b-card>
                        <b-card-body v-if="product.hasFormattedAnno">
                            <v-row v-for="(value, param) of annotationOrig" :key="param">
                                <v-col cols="7">
                                    {{param}}
                                    <hr style="width: 180%;">
                                </v-col>
                                <v-col align="right">
                                    <strong>{{value}}</strong>
                                </v-col>
                            </v-row>
                        </b-card-body>

                        <b-card-body v-else>
                            <v-row v-for="(value, param) of formattedAnno" :key="param">
                                <v-col cols="7">
                                    {{param}}
                                    <hr style="width: 180%;">
                                </v-col>
                                <v-col align="right">
                                    <strong>{{value}}</strong>
                                </v-col>
                            </v-row>
                        </b-card-body>
                    </b-card>
                </v-col>

                <v-col cols="6">
                    <v-card max-width="600">

                        <!--ЗАМИНИТЬ НА DIV И SPAN-->
                        <v-list-item>
                            <v-list-item-content>
                                <v-list-item-title class="headline">{{ product.originalName }}</v-list-item-title>
                                <v-list-item-subtitle>{{ product.singleTypeName }}</v-list-item-subtitle>
                            </v-list-item-content>
                        </v-list-item>

                        <!--Если одно фото-->
                        <div v-if="!product.pics">
                            <a @mouseover="this.style.cursor='pointer'">
                                <v-img class="white--text" contain eager max-height="300" :src="product.pic" alt="Bad Link" @click.stop="picDialog = true"></v-img>
                            </a>

                            <v-dialog v-model="picDialog" max-width="80%">
                                <v-card>
                                    <v-img class="white--text" height="1000" contain eager :src="product.pic" alt="Bad Link" @click.stop="picDialog = true"></v-img>
                                </v-card>
                            </v-dialog>
                        </div>

                        <!--Если несколько фото-->
                        <div v-else>
                            <a @mouseover="this.style.cursor='pointer'">
                                <v-img class="white--text" contain max-height="300" :src="mainPhoto" alt="Bad Link" @click.stop="picDialog = true"></v-img>
                            </a>

                            <v-dialog v-model="picDialog" max-width="80%">
                                <v-card>
                                    <v-img class="white--text" height="1000" contain :src="mainPhoto" alt="Bad Link" @click.stop="picDialog = true"></v-img>
                                </v-card>
                            </v-dialog>

                            <v-card-actions >
                                <v-tabs height="100" center-active show-arrows :centered="true" slider-color="#e52d00" background-color="#ffffff">
                                    <v-tab v-for="(pic, index) of fewPics" :key="pic">
                                        <v-img width="20" class="white--text" contain height="100" :src="pic" @click="activePhoto(pic)"></v-img>
                                    </v-tab>
                                </v-tabs>
                            </v-card-actions>
                        </div>
                    </v-card>

                    <v-row>
                        <v-col>
                            <v-card >
                                <v-row>
                                    <v-col cols="4">
                                        <v-card-title>{{ product.finalPrice.toLocaleString('ru-RU') }} ₽</v-card-title>
                                    </v-col>
                                    <v-col class="mr-5">
                                        <v-card-text>За покупку будет зачисленно <strong>{{ product.bonus }} </strong> баллов!</v-card-text>
                                    </v-col>
                                </v-row>

                                <v-card-actions v-if="!productInOrder(product.productID)">
                                    <v-btn text outlined block color="#e52d00" @click="addToOrder(product.productID)">
                                        В корзину
                                    </v-btn>
                                </v-card-actions>
                                <v-card-actions v-else>
                                    <v-btn class="goToOrderButton" block @click="toOrder()" style="background-color: #e52d00; color: #ffffff">
                                        Перейти в корзину
                                    </v-btn>
                                </v-card-actions>
                            </v-card>
                        </v-col>
                    </v-row>
                </v-col>
            </v-row>
        </v-container>
    </div>
</template>

<script>
    import axios from 'axios'
    /*REFACTORING NEEDED!*/
    export default {
        data() {
            return {
                product: '',
                linkBack: '',
                anno: [],
                annoOrig: [],
                picDialog: false,
                formattedAnno:{},
                annotationOrig: {},
                fewPics: [],
                mainPhoto: ''
            }
        },
        beforeCreate() {
            /*REFACTORING NEEDED!*/
            /*РАЗДЕЛИТЬ НА МЕТОДЫ!*/

            const productID = (decodeURI(window.location.href).substr(decodeURI(window.location.href).lastIndexOf('/')+1));
            let url = '/api/products/show/' + productID;

            axios.get(url).then(response => {
                this.product = response.data
                this.anno =  (this.product.shortAnnotation.split(';').map(String)).filter(Boolean)
                this.annoOrig = this.anno =  (this.product.annotation.split(';').map(String)).filter(Boolean)

                this.linkBack = '/products/'+ (this.product.productGroup).toLowerCase();

                this.anno.forEach(value => {
                    if (value.includes(':')) {
                        let key = value.substr(0, value.indexOf(':'))
                        let val = value.substr(value.indexOf(':') + 1).trim()
                        this.formattedAnno[key] = val
                    }
                    else {
                        this.formattedAnno[value] = ''
                    }
                })

                this.annoOrig.forEach(value => {
                    if (value.includes(':')) {
                        let key = value.substr(0, value.indexOf(':'))
                        let val = value.substr(value.indexOf(':') + 1).trim()
                        this.annotationOrig[key] = val
                    }
                    else {
                        this.annotationOrig[value] = ''
                    }
                })

                this.fewPics = this.product.pics.split(';').filter(pic => pic)
                this.mainPhoto = this.fewPics[0]
                console.log(this.fewPics)
            });
        },
        methods: {
            addToOrder(productID) {
                const url = '/api/order/addProduct'
                axios.post(url, productID).then(response => {
                    this.$store.dispatch('addOrderedProduct', productID)
                    this.$store.dispatch('updateOrder', response.data)
                })
            },
            productInOrder(productID) {
                return this.$store.state.orderedProducts.includes(productID)
            },
            toOrder() {
                this.$router.push('/order')
            },
            activePhoto(pic) {
                this.mainPhoto = pic
            }
        }
    }
</script>

<style scoped></style>

