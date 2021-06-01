<template>
    <v-card outlined tile class="mx-2 mt-3 mr-4 d-flex flex-column" width="300">
        <!--Главное фото + открыть фото в диалоге-->
        <div class="p-3">
            <a>
                <v-img class="white--text" eager contain height="150px" :src="product.pic"  alt="Bad Link" @click.stop="picDialog = true"></v-img>

                <!--AdminMode-->
                <div v-if="isAdminMode">
                    <v-text-field
                            label="Ссылка на картинку"
                            single-line
                            v-model="link"
                    ></v-text-field>
                    <v-btn block small outlined tile v-if="linkNotEmpty" @click="sendImageLinkToServer(product.productID)">Добавить фото</v-btn>
                </div>
                <!---->
            </a>
            <v-dialog v-model="picDialog" max-width="80%">
                <v-card>
                    <v-img class="white--text" height="1000" contain :src="product.pic" alt="Bad Link" @click.stop="picDialog = true"></v-img>
                </v-card>
            </v-dialog>
        </div>

        <b-card-sub-title class="ml-5 mt-1">
            {{ toUpperCase(product.singleTypeName) }}
        </b-card-sub-title>

        <v-card-text>
            <router-link :to=showProductInfo>
                <div v-if="product.modelName" class="align-top fill-height headline">{{product.brand}} {{product.modelName}}</div>
                <div v-else class="align-top fill-height headline">{{product.originalName}} </div>
            </router-link>
        </v-card-text>

        <v-spacer></v-spacer>

        <v-card-text >
            <ul class="pl-1">
                <li v-for="anno in annotations" v-if="anno" style="list-style-type: none;" >
                        <span class="font-weight-light">
                            {{anno}}
                        </span>
                </li>
            </ul>
        </v-card-text>

        <v-spacer></v-spacer>

        <div class="d-flex align-baseline" v-if="!isAdminMode">
            <v-card-text style="padding-top: 25px;" class="flex-grow-1">
                <h5>{{product.finalPrice.toLocaleString('ru-RU')}}₽</h5>
            </v-card-text>

            <v-card-actions style="align-items: end; padding-right: 15px;">
                <v-btn class="flex-grow-1" text outlined color="#e52d00" v-if="!productInOrder(product.productID)" @click="addToOrder(product.productID)">
                    В корзину
                </v-btn>
                <v-btn class="flex-grow-1" style="background-color: #e52d00; color: #ffffff" v-else @click="toOrder()">
                    Перейти в корзину
                </v-btn>
            </v-card-actions>
        </div>

        <!--Редактирование цены-->
        <div v-else>
            <v-card-actions>
                <v-row>
                    <v-col cols="7">
                        <v-text-field
                                label="Цена"
                                v-model="price"
                        ></v-text-field>
                    </v-col>
                    <v-col>
                        <v-btn outlined small class="mt-4" @click="setCustomPrice(product.productID)">Обновить</v-btn>
                    </v-col>
                </v-row>
            </v-card-actions>
            <v-card-actions v-if="product.priceModified">
                <v-btn block @click="restoreDefaultPrice(product.productID)">Цена по умолчанию</v-btn>
            </v-card-actions>
        </div>
    </v-card>
</template>

<script>
    import axios from 'axios'
    export default {
        props: ['product'],
        data() {
            return {
                showProductInfo: '/products/product/' + this.product.productID,
                dialog: false,
                annotations: (this.product.shortAnnotation.split(';').map(String)).slice(0,8),
                picDialog: false,
                rules: [
                    value => !value || value.size < 2000000 || 'Изображение должно быть меньше 2 MB!',
                ],
                link:'',
                price: this.product.finalPrice
            }
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
            sendImageLinkToServer(productID) {
                const data = {
                    'link': this.link,
                    'productID': productID
                }
                axios.post('/admin/downloadImage', data, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then((response) => {
                    console.log(response.data)
                })
            },
            setCustomPrice(productID) {
                const priceUpdate = {
                    'productID': productID,
                    'newPrice': this.price
                }
                axios.post('/admin/setCustomPrice', priceUpdate).then((response) => {
                    this.product = response.data
                })
            },
            restoreDefaultPrice(productID) {
                console.log(productID)
                axios.post('/admin/restoreDefaultPrice', productID, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then((response) => {
                    this.product = response.data
                    this.price = response.data.finalPrice
                })
            },
            toUpperCase(param) {
                return param.charAt(0).toUpperCase() + param.substr(1)
            }
        },
        computed: {
            isAdminMode() {
                return this.$store.state.adminMode
            },
            linkNotEmpty() {
                return this.link !== ''
            }
        }
    }
</script>

<style scoped>
    .goToOrderButton {
        background-color: #e52d00;
        color: #fafafa;
    }
</style>
