<template>
    <v-container>
        <v-layout justify-center align-center>
            <v-list subheader width="100%">
                <v-subheader style="background-color: #f2f2f2;"><h3>Завершенные заказы</h3></v-subheader>
                <v-list-item v-for="order of completedOrders" :key="order.orderID" style="background-color: #f2f2f2;">
                    <v-card outlined width="100%" class="mb-3">
                        <v-card-title>
                            <v-row>
                                <v-col cols="2">
                                    Заказ №{{order.orderID}}
                                </v-col>
                                <v-col>
                                    от {{order.openDate.replace('T',' ')}}
                                </v-col>
                            </v-row>
                        </v-card-title>

                        <v-card-text>
                            <v-row>
                                <v-col class="subtitle-1 black--text" v-if="order.user !== null">
                                    ФИО: {{order.user.lastName}} {{order.user.firstName}} {{order.user.patronymic}}
                                </v-col>
                                <v-col class="subtitle-1 black--text" v-else>
                                    ФИО: {{order.clientName}}
                                </v-col>
                            </v-row>
                            <v-row>
                                <v-col class="subtitle-1 black--text">
                                    Телефон: +{{order.clientMobile}}
                                </v-col>
                            </v-row>
                            <v-row>
                                <v-col class="subtitle-1 black--text">
                                    Сумма заказа: {{order.totalPrice.toLocaleString('ru-RU')}} ₽
                                </v-col>
                            </v-row>
                            <v-row v-if="order.discountPrice !== null">
                                <v-col class="subtitle-1 black--text" >
                                    <strong>Сумма заказа со скидкой: {{(order.discountPrice.toLocaleString('ru-RU'))}} ₽</strong>
                                </v-col>
                                <v-col class="subtitle-1 black--text">
                                    Скидка: {{order.discount}}
                                </v-col>
                            </v-row>
                        </v-card-text>

                        <v-divider></v-divider>

                        <v-subheader>Заказанные товары</v-subheader>
                        <div v-for="product in order.orderedList" :key="product.productID">
                            <v-row class="ml-2">
                                <v-col cols="1">
                                    <v-img :src="product.pic" height="50" contain></v-img>
                                </v-col>

                                <v-col cols="7" style="padding-top: 25px;">
                                    <router-link :to="'/products/product/'+product.productID" style="color: black"><span v-text="product.productName"></span></router-link>
                                </v-col>

                                <v-col cols="2" style="padding-top: 25px;">
                                    <span><strong>{{(product.productPrice * product.productAmount).toLocaleString('ru-RU')}}</strong> ₽</span>
                                </v-col>

                                <v-col cols="2" style="padding-top: 25px;">
                                    <span>за <strong>{{product.productAmount}} шт.</strong></span>
                                </v-col>
                            </v-row>
                        </div>

                        <v-divider></v-divider>

                        <v-card-actions>
                            <v-row>
                                <v-col>
                                    <v-btn outlined block color="black" dark @click="cancelCompleteOrder(order.orderID)">
                                        Отменить завершение
                                        <v-icon dark right>mdi-cancel</v-icon>
                                    </v-btn>
                                </v-col>
                                <v-col>
                                    <v-btn color="red" block outlined dark @click="deleteOrder(order.orderID)">
                                        Удалить
                                        <v-icon dark right>mdi-close</v-icon>
                                    </v-btn>
                                </v-col>
                            </v-row>
                        </v-card-actions>
                    </v-card>
                </v-list-item>
            </v-list>
        </v-layout>
    </v-container>
</template>

<script>
    import axios from "axios";
    export default {
        data() {
            return {
                completedOrders: [],
            }
        },
        created() {
            axios.get('/admin/completedOrders').then(response => {
                this.completedOrders = response.data
            })
        }
    }
</script>

