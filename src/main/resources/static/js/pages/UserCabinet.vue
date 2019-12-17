<template>
    <div v-if="userConfirmed">
        <v-navigation-drawer absolute permanent>

            <template v-slot:prepend>
                <v-list-item two-line>
                    <v-list-item-content>
                        <v-list-item-title>{{lastName}} {{username}} {{patronymic}}</v-list-item-title>
                        <v-list-item-subtitle><strong>+{{mobile}}</strong></v-list-item-subtitle>
                        <v-list-item-subtitle>Бонусов: <strong>{{userBonus}}</strong></v-list-item-subtitle>
                    </v-list-item-content>
                </v-list-item>
            </template>

            <v-divider></v-divider>

            <v-list dense>
                <v-list-item @click="showCurrentOrdersPage()">
                    <v-list-item-icon>
                        <v-icon>mdi-clock-outline</v-icon>
                    </v-list-item-icon>

                    <v-list-item-content>
                        <v-list-item-title>Текущие заказы</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <v-list-item @click="showCompletedOrdersPage()">
                    <v-list-item-icon>
                        <v-icon>mdi-check-outline</v-icon>
                    </v-list-item-icon>

                    <v-list-item-content>
                        <v-list-item-title>Завершенные заказы</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
            </v-list>

            <v-divider></v-divider>

            <v-card-actions>
                <v-btn block tile @click="logout">
                    <span>Выход</span>
                    <v-icon>mdi-logout</v-icon>
                </v-btn>
            </v-card-actions>

        </v-navigation-drawer>
        <v-content>
            <v-container fluid fill-height>
                <v-container v-if="activeContainerCurrent">
                    <v-layout justify-center align-center>

                        <v-list subheader width="100%" style="background-color: #f2f2f2;">
                            <v-subheader><h3>Принятые заказы</h3></v-subheader>

                            <v-list-item-content v-for="order of acceptedOrders" :key="order.orderID">
                                <v-card outlined width="100%" class="mb-6">

                                    <v-row>
                                        <v-col cols="7">
                                            <v-card-title>
                                                Заказ №{{order.orderID}} от {{order.openDate.replace('T',' ')}}
                                            </v-card-title>
                                        </v-col>

                                        <v-col>
                                            <v-card-title>
                                                Статус заказа: <div class="ml-3"><span v-if="!order.confirmed" style="color: #5181b8">В обработке</span><span v-else style="color:#5fb053;">Подтвержден</span></div>
                                            </v-card-title>
                                        </v-col>
                                    </v-row>


                                    <v-card-text class="pt-0 pb-0">
                                        <div class="my-4 subtitle-1 black--text" v-if="order.discountPrice === null">
                                            Сумма заказа: <strong>{{order.totalPrice.toLocaleString('ru-RU')}} ₽</strong>
                                        </div>
                                        <div class="my-4 subtitle-1 black--text" v-else>
                                            Сумма заказа: <strong>{{order.discountPrice.toLocaleString('ru-RU')}} ₽</strong>
                                        </div>
                                    </v-card-text>

                                    <v-card-text class="pt-0 pb-0">
                                        <div class="my-4 subtitle-1 black--text" v-if="order.user !== null">
                                            Бонусов за заказ: <strong>{{order.totalBonus}}</strong>
                                        </div>
                                    </v-card-text>

                                    <v-divider></v-divider>


                                    <!--<v-simple-table>
                                        <template v-slot:default>
                                            <tbody>
                                            <tr  v-for="product in order.orderedList" :key="product.productID">
                                                <td>
                                                    <v-img :src="product.pic" height="50" contain></v-img>
                                                </td>
                                                <td>
                                                    <router-link :to="'/products/product/'+product.productID" style="color: black"><span v-text="product.productName"></span></router-link>
                                                </td>
                                                <td>
                                                    <span><strong>{{(product.productPrice * product.productAmount).toLocaleString('ru-RU')}}</strong> ₽</span>
                                                </td>
                                                <td>
                                                    <span>за <strong>{{product.productAmount}} шт.</strong></span>
                                                </td>

                                            </tr>
                                            </tbody>
                                        </template>
                                    </v-simple-table>-->
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


                                    <!--<v-list subheader>
                                        <v-subheader>Заказанные товары</v-subheader>
                                        <v-list-item selectable v-for="product in order.orderedList" :key="product.productID">
                                            <v-list-item-avatar>
                                                <v-img :src="product.pic"></v-img>
                                            </v-list-item-avatar>

                                            <v-list-item-content>
                                                <router-link :to="'/products/product/'+product.productID" style="color: black"><v-list-item-title v-text="product.productName"></v-list-item-title></router-link>
                                            </v-list-item-content>

                                            <v-list-item-content>
                                                <v-list-item-title>
                                                    <span><strong>{{(product.productPrice * product.productAmount).toLocaleString('ru-RU')}}</strong> ₽</span>
                                                </v-list-item-title>
                                            </v-list-item-content>

                                            <v-list-item-content>
                                                <v-list-item-title>
                                                    <span>за <strong>{{product.productAmount}} шт.</strong></span>
                                                </v-list-item-title>
                                            </v-list-item-content>

                                        </v-list-item>
                                    </v-list>-->
                                </v-card>
                            </v-list-item-content>
                        </v-list>
                    </v-layout>
                </v-container>
                <v-container v-if="activeContainerCompleted">
                    <v-layout justify-center align-center>
                        <v-list subheader width="100%" style="background-color: #f2f2f2;">
                            <v-subheader><h3>Завершенные заказы</h3></v-subheader>
                            <v-list-item v-for="order of completedOrders" :key="order.orderID">
                                <v-card outlined width="100%" class="mb-6">

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

                                    <v-card-text class="pt-0 pb-0">
                                        <div class="my-4 subtitle-1 black--text">
                                            Сумма заказа: <strong>{{order.totalPrice.toLocaleString('ru-RU')}} ₽</strong>
                                        </div>
                                    </v-card-text>

                                    <v-card-text class="pt-0 pb-0">
                                        <div class="my-4 subtitle-1 black--text">
                                            Зачисленно бонусов: <strong>{{order.totalBonus}}</strong>
                                        </div>
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

                                    <!--<v-list subheader>
                                        <v-subheader>Заказанные товары</v-subheader>
                                        <v-list-item v-for="product in order.orderedList" :key="product.productID">
                                            <v-list-item-avatar>
                                                <v-img :src="product.pic"></v-img>
                                            </v-list-item-avatar>

                                            <v-list-item-content>
                                                <v-list-item-title v-text="product.productName"></v-list-item-title>
                                            </v-list-item-content>

                                            <v-list-item-content>
                                                <v-list-item-title>
                                                    <span><strong>{{(product.productPrice * product.productAmount).toLocaleString('ru-RU')}}</strong> ₽</span>
                                                </v-list-item-title>
                                            </v-list-item-content>

                                            <v-list-item-content>
                                                <v-list-item-title>
                                                    <span>за <strong>{{product.productAmount}} шт.</strong></span>
                                                </v-list-item-title>
                                            </v-list-item-content>
                                        </v-list-item>
                                    </v-list>-->
                                </v-card>
                            </v-list-item>
                        </v-list>
                    </v-layout>
                </v-container>
            </v-container>
        </v-content>
    </div>
</template>

<script>
    import axios from 'axios'
    export default {
        data() {
            return {
                mobile:     this.$store.state.currentUser.username,
                username:   this.$store.state.currentUser.firstName,
                lastName:   this.$store.state.currentUser.lastName,
                patronymic: this.$store.state.currentUser.patronymic,
                userBonus:  '',
                acceptedOrders: [],
                completedOrders: [],
                activeContainerCurrent: true,
                activeContainerCompleted: false,
                userConfirmed: false
            }
        },
        created() {
            this.loadAcceptedOrders()
            this.loadCompletedOrders()
            this.showUserBonus()
        },
        mounted() {
            this.userConfirmed = true
        },
        methods: {
            logout() {
                axios.post('http://localhost:9000/user/logout').then(() => {
                    this.$store.dispatch('logout')
                    this.$store.dispatch('removeOrder')
                    this.$store.dispatch('clearOrderedProducts')
                    this.$router.push('/')
                })
            },
            loadAcceptedOrders() {
                axios.get('/api/order/getAcceptedOrders').then((response) => {
                    this.acceptedOrders = response.data
                })
            },
            loadCompletedOrders() {
                axios.get('/api/order/getCompletedOrders').then((response) => {
                    this.completedOrders = response.data
                })
            },
            showCurrentOrdersPage() {
                this.activeContainerCurrent = true
                this.activeContainerCompleted = false
            },
            showCompletedOrdersPage() {
                this.activeContainerCurrent = false
                this.activeContainerCompleted = true
            },
            showUserBonus() {
                axios.get('/api/order/showUserBonus').then((response) => {
                    console.log(response)
                    this.$store.dispatch('setUserBonus', response.data)
                    this.userBonus = response.data
                })
            }
        },
        beforeCreate() {
            axios.post('/auth/noUser').then(noUser => {
                if (noUser.data === true) {
                    this.$router.push('/login')
                }
            })
        }
    }
</script>

<style scoped></style>