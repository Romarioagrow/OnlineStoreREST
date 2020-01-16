<template>
    <div>
        <v-container class="pt-0">
            <v-row>
                <v-col class="pt-0">
                    <v-toolbar flat>
                        <v-toolbar-items>

                            <router-link :to="'/catalog/' + product.productCategory">
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
                        </v-toolbar-items>
                    </v-toolbar>
                </v-col>
            </v-row>

            <v-row>
                <v-col>
                    <b-card>
                        <!--АННОТАЦИЯ-->
                        <b-card-body>
                            <v-row v-for="[annoKey, annoVal] of this.annotation" :key="annoKey">
                                <v-col cols="7">
                                    {{annoKey}}
                                </v-col>
                                <v-col align="right">
                                    <strong>{{annoVal}}</strong>
                                </v-col>
                                <hr style="width: 180%;">
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
                                        <v-card-title>{{ finalPrice.toLocaleString('ru-RU') }} ₽</v-card-title>
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
    export default {
        data() {
            return {
                product: '',
                linkBack: '',
                annotation: new Map(),
                picDialog: false,
                fewPics: [],
                mainPhoto: '',
                finalPrice: ''
            }
        },
        beforeCreate() {
            const productID = (decodeURI(window.location.href).substr(decodeURI(window.location.href).lastIndexOf('/')+1));
            let url = '/api/products/show/' + productID;

            axios.get(url).then(response => {
                this.product = response.data
                this.linkBack = '/products/'+ (this.product.productGroup).toLowerCase();
                this.finalPrice = this.product.finalPrice

                this.product.shortAnnotation
                    .split(';')
                    .filter(anno => anno)
                    .forEach(anno => {
                        let annoKey = anno.includes(':') ? anno.substr(0, anno.indexOf(':')) : anno
                        let annoVal = anno.includes(':') ? anno.substr(anno.indexOf(':') + 1) : ''
                        this.annotation.set(annoKey, annoVal)
                    })

                this.mainPhoto = this.fewPics[0]
                if (this.product.pics) {
                    this.fewPics = this.product.pics.split(';').filter(pic => pic)
                }
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


