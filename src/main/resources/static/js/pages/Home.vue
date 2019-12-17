<template>
    <div>
        <v-progress-linear indeterminate color="#e52d00" v-if="false"></v-progress-linear>
        <b-container fluid class="flu">

            <v-card>
                <v-carousel hide-delimiters cycle>
                    <v-carousel-item v-for="(image, i) in items" :key="image">
                        <v-img contain eager :src="items[i]" alt="Bad Link"></v-img>
                    </v-carousel-item>
                </v-carousel>


                <router-link to="/catalog" class="catalogLink">
                    <v-btn block outlined color="#e52d00">
                        Каталог товаров
                    </v-btn>
                </router-link>
            </v-card>

            <!--<v-img src="../../img/products/03_00018122.png"></v-img>
            &lt;!&ndash;<v-img src="./img/products/03_00018122.png"></v-img>
            <v-img src="./img/logo.png"></v-img>
            <v-img src="./img/navpic.png"></v-img>
            <v-img src="./img/5749028_0.jpg"></v-img>
            <v-img src="./img/Blindspot-tv-show-bathtub-tattoo-wallpaper.jpg"></v-img>
            <v-img src="./img/Screenshot_1.png"></v-img>&ndash;&gt;-->

            <v-card class="mt-5" >
                <v-card-title>
                    Популярные товары
                </v-card-title>

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
                                <v-img class="white--text" style="margin-top:3%" contain height="120px" :src="product.pic"></v-img>

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
        <!-- <FooterDiv></FooterDiv>-->
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
                    'http://vsalde.ru/uploads/posts/2017-11/1510154417_a4-sale-05-700.jpg',
                    'http://vsalde.ru/uploads/posts/2017-11/1510154473_a4-sale-01-700.jpg',
                    'http://vsalde.ru/uploads/posts/2017-11/1510154415_a4-sale-02-700.jpg',
                    'http://vsalde.ru/uploads/posts/2017-11/1510154418_a4-sale-03-700.jpg',
                    'http://vsalde.ru/uploads/posts/2017-11/1510154477_a4-sale-04-700.jpg',
                ],
                recentProducts: [],
                popularProducts: [
                    '02.02.08.0A.000025',
                    '02.02.04.02.000066',
                    '01.01.02.05.04.067',
                    '01.01.02.01.02530',
                    '01.05.01.000001258',
                    '07.03.01.000001600',
                    '03.01.01.02.001580',
                    '03.02.01.02.001646',
                    '03.02.01.01.001333',
                    '03.03.01.000002028'
                ],
            }
        },
        created() {
            axios.get('/api/products/getRecentProducts').then(response => {
                this.recentProducts = response.data
            })
            axios.get('/api/products/popularProducts').then(response => {
                this.popularProducts = response.data
            })
        },
        methods: {
            goToProduct(productID) {
                this.$router.push('/products/product/' + productID)
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
</style>