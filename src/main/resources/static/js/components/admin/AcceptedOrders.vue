<template>
    <v-container>
        <v-subheader >
            <h3>Принятые заказы</h3>
        </v-subheader>

        <v-text-field
                v-model="acceptedOrderSearch"
                label="Телефон, ФИО, №Заказа"
                solo
                @input="searchAcceptedOrders()"
        ></v-text-field>

        <v-row v-for="order of acceptedOrders" :key="order.orderID">
            <v-card outlined width="100%" class="mb-3">
                <v-card-title>
                    <v-row>
                        <v-col cols="2">
                            Заказ №{{order.orderID}}
                        </v-col>
                        <v-col>
                            от {{order.openDate.replace('T',' ')}}
                        </v-col>
                        <v-col cols="6">
                            <h3 v-if="order.confirmed" style="color: #5fb053">Подтвержден, ожидает оплаты</h3>
                            <h3 v-else style="color: red;">Не подтвержден</h3>
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
                            <div v-if="!order.confirmed">
                                <v-btn block outlined color="primary" dark @click="confirmOrder(order.orderID)">
                                    Подтвердить
                                    <v-icon dark right>mdi-check</v-icon>
                                </v-btn>
                            </div>
                            <div v-else>
                                <v-btn block color="primary" dark @click="cancelConfirmOrder(order.orderID)">
                                    Отменить подтверждение
                                    <v-icon dark right>mdi-cancel</v-icon>
                                </v-btn>
                            </div>
                        </v-col>
                        <v-col v-if="order.confirmed">
                            <v-btn block outlined color="green" dark @click="completeOrder(order.orderID)">
                                Завершить
                                <v-icon dark right>mdi-checkbox-marked-circle</v-icon>
                            </v-btn>
                        </v-col>
                        <v-col>
                            <v-btn block color="red" dark @click="deleteOrder(order.orderID)">
                                Удалить
                                <v-icon dark right>mdi-close</v-icon>
                            </v-btn>
                        </v-col>
                    </v-row>
                </v-card-actions>
            </v-card>
        </v-row>
    </v-container>
</template>

<script>
    import axios from "axios";
    export default {
        data() {
            return {
                acceptedOrderSearch: '',
                acceptedOrders: [],
            }
        },
        created() {
            axios.get('/admin/acceptedOrders').then(response => {
                this.acceptedOrders = response.data
                console.log(this.acceptedOrders)
            })
        },
        methods: {
            confirmOrder(orderID) {
                axios.post('admin/confirmOrder', orderID, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(() => {
                    axios.get('/admin/acceptedOrders').then(response => {
                        this.acceptedOrders = response.data
                    })
                })
            },
            cancelConfirmOrder(orderID) {
                axios.post('admin/cancelConfirmOrder', orderID, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(() => {
                    axios.get('/admin/acceptedOrders').then(response => {
                        this.acceptedOrders = response.data
                    })
                })
            },
            completeOrder(orderID) {
                axios.post('admin/completeOrder', orderID, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then(() => {
                    axios.get('/admin/acceptedOrders').then(response => {
                        this.acceptedOrders = response.data
                    })
                })
            },
            deleteOrder(orderID) {
                axios.post('admin/deleteOrder', orderID, {
                    headers: {
                        'Content-Type': 'application/json'
                    }
                }).then((response) => {
                    this.acceptedOrders = response.data
                })
            },
            searchAcceptedOrders() {
                if (this.acceptedOrderSearch === '') {
                    axios.get('/admin/acceptedOrders').then(response => {
                        this.acceptedOrders = response.data
                    })
                }

                const headers = {
                    'Content-Type': 'application/json',
                }

                axios.post('admin/searchAcceptedOrders', this.acceptedOrderSearch, {
                    headers: headers
                })
                    .then((response) => {
                        console.log(response.data)
                        if (response.data !== null) {
                            this.acceptedOrders = response.data
                        }

                    })
                    .catch((error) => {
                        console.log(error)
                    })
            }
        }
    }
</script>

