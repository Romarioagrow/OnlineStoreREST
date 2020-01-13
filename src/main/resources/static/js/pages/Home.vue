<template>
    <div>
        <v-progress-linear indeterminate color="#e52d00" v-if="false"></v-progress-linear>
        <b-container fluid class="flu">



            <v-card>
                <v-carousel hide-delimiters cycle>
                    <v-carousel-item v-for="(image, i) in items" :key="image">
                        <v-img eager contain height="500" :src="items[i]" alt="Bad Link"></v-img>
                    </v-carousel-item>
                </v-carousel>

                <router-link to="/catalog" class="catalogLink">
                    <v-btn block outlined color="#e52d00">
                        Каталог товаров
                    </v-btn>
                </router-link>
            </v-card>

            <!--Популярные товары-->
            <v-card class="mt-5" >
                <v-card-title>
                    Популярные товары
                </v-card-title>

                <v-card-actions v-if="isAdminMode">
                    <div>
                        <v-btn color="primary" dark @click.stop="dialog = true">
                            Изменить ссылки
                        </v-btn>
                        <v-dialog v-model="dialog" max-width="750" >
                            <v-card class="chartAreaWrapper">
                                <v-card-actions>
                                    <v-row class="ml-2">
                                        <v-col cols="9">
                                            <v-text-field
                                                    label="ID товара"
                                                    v-model="addPopularID"
                                            ></v-text-field>
                                        </v-col>
                                        <v-col>
                                            <v-btn outlined class="mt-4" @click="addPopularProduct()">
                                                Добавить
                                            </v-btn>
                                        </v-col>
                                    </v-row>
                                </v-card-actions>
                                <v-card-actions>
                                    <v-simple-table class="w-100">
                                        <tbody>
                                        <tr v-for="product in popularProducts" :key="product.productID">
                                            <td>{{ product.productID }}</td>
                                            <td>{{ product.fullName }}</td>
                                            <td>
                                                <v-icon @click="deleteItem(product.productID)">
                                                    mdi-delete-forever
                                                </v-icon>
                                            </td>
                                        </tr>
                                        </tbody>
                                    </v-simple-table>
                                </v-card-actions>
                            </v-card>
                        </v-dialog>
                    </div>
                </v-card-actions>

                <v-card-actions>
                    <v-tabs height="300" center-active show-arrows :centered="true" slider-color="#e52d00" background-color="#ffffff">
                        <v-tab v-for="(product, index) of popularProducts" :key="product.productID">
                            <v-card height="280" @click="goToProduct(product.productID)">
                                <v-img class="white--text" contain height="130px" :src="product.pic"></v-img>

                                <v-spacer></v-spacer>

                                <v-card-text class="body-1 ">
                                    {{product.fullName}}
                                </v-card-text>

                                <v-spacer></v-spacer>

                                <v-card-text>
                                    <div class="subtitle-2">{{product.finalPrice.toLocaleString('ru-RU')}}₽</div>
                                </v-card-text>
                            </v-card>
                        </v-tab>
                    </v-tabs>
                </v-card-actions>
            </v-card>

            <v-card class="mt-5">
                <v-card-title>
                    Недавно заказанные товары
                </v-card-title>
                <v-card-actions>
                    <v-tabs height="300" center-active show-arrows fixed-tabs :centered="true" slider-color="#e52d00" background-color="#ffffff">
                        <v-tab v-for="(product, index) of recentProducts" :key="product.productID">

                            <v-card height="280" @click="goToProduct(product.productID)">
                                <v-img class="white--text" style="margin-top:3%" contain eager height="120px" :src="product.pic"></v-img>

                                <v-spacer></v-spacer>

                                <v-card-text class="body-1">
                                    {{product.productName}}
                                </v-card-text>

                                <v-spacer></v-spacer>

                                <v-card-text>
                                    <div class="subtitle-2">{{product.productPrice.toLocaleString('ru-RU')}}₽</div>
                                </v-card-text>
                            </v-card>
                        </v-tab>
                    </v-tabs>
                </v-card-actions>
            </v-card>
        </b-container>
    </div>
</template>

<script>
    import axios from 'axios'
    import FooterDiv from "components/app/Footer.vue";
    export default {
        components: {
            FooterDiv
        },
        data () {
            return {
                items: [
                    'https://sun9-58.userapi.com/c857416/v857416537/1202c7/p_JErbHonRQ.jpg',
                    'https://sun9-10.userapi.com/c857132/v857132445/737ac/KIjY-iznuIY.jpg',
                    'https://sun9-71.userapi.com/c855428/v855428084/18a72a/lOlZckKYZRE.jpg',
                    'https://sun9-25.userapi.com/c858120/v858120638/f651a/4Dzmbu8YWZY.jpg',
                    'https://sun9-36.userapi.com/c858228/v858228549/e98e0/weobbboJygQ.jpg',
                    'https://sun9-24.userapi.com/c855136/v855136742/14224d/qfw4y3PZxpA.jpg',
                    'https://sun9-63.userapi.com/c857328/v857328129/f631/QtnoqTeAA44.jpg',
                ],
                recentProducts: [],
                popularProducts: [],
                dialog: false,
                addPopularID: ''
            }
        },
        created() {
            axios.get('/api/products/getPopularProducts').then(response => {
                this.popularProducts = response.data
            })
            axios.get('/api/products/getRecentProducts').then(response => {
                this.recentProducts = response.data
            })
        },
        methods: {
            goToProduct(productID) {
                this.$router.push('/products/product/' + productID)
            },
            addPopularProduct() {
                axios.post('/api/products/addPopularProduct', this.addPopularID, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(response => {
                    this.popularProducts = response.data
                })
            },
            deleteItem(productID) {
                axios.post('/api/products/deletePopularProduct', productID, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(response => {
                    this.popularProducts = response.data
                })
            }
        },
        computed: {
            isAdminMode() {
                return this.$store.state.adminMode
            }
        }
    }
</script>

<style scoped>
    .flu {
        width: 60%;
    }
    .catalogLink:hover {
        text-decoration: none;
    }
    .chartAreaWrapper {
        overflow-x: hidden;
    }
</style>