<template>
    <v-content>
        <v-progress-linear indeterminate color="#e52d00" v-if="loading"></v-progress-linear>
        <b-container fluid style="width: 90%;" v-else>
            <v-row>
                <v-col cols="8" v-if="orderHasProducts">
                    <v-card outlined>
                        <v-list subheader>
                            <v-subheader>Товары в корзине</v-subheader>

                            <v-list-item v-for="[product, amount] of orderedProducts" :key="product.productID" @click="" style="min-height: 5rem;">
                                <v-list-item-avatar horizontal class="ml-2">
                                    <v-img contain :src="product.pic"></v-img>
                                </v-list-item-avatar>

                                <v-list-item-content class="ml-12">
                                    <router-link :to="'/products/product/' + product.productID">
                                        <v-list-item-title v-text="product.fullName" style="color: black"></v-list-item-title>
                                    </router-link>
                                </v-list-item-content>

                                <v-list-item-content class="ml-12">
                                    <v-list-item-title>
                                        <span><strong>{{(product.finalPrice * amount).toLocaleString('ru-RU')}}</strong> ₽</span>
                                    </v-list-item-title>
                                </v-list-item-content>

                                <v-list-item-action class="ml-12">
                                    <div class="my-2">
                                        <v-btn text small icon @click="decreaseAmount(product.productID)" :disabled="amount === 1">
                                            <v-icon>mdi-minus</v-icon>
                                        </v-btn>
                                        <span>{{amount}}</span>
                                        <v-btn text small icon @click="increaseAmount(product.productID)">
                                            <v-icon>mdi-plus</v-icon>
                                        </v-btn>
                                    </div>
                                </v-list-item-action>

                                <v-list-item-icon>
                                    <v-btn icon @click="deleteProduct(product.productID)">
                                        <v-icon>mdi-close-circle</v-icon>
                                    </v-btn>
                                </v-list-item-icon>

                            </v-list-item>
                        </v-list>
                    </v-card>
                </v-col>

                <v-col>
                    <v-card outlined v-if="productAmount !== 0">

                        <v-card-text class="pb-0">
                            <v-row>
                                <v-col>
                                    <p class="display-1">Сумма заказа <span class="text--primary">{{totalPrice.toLocaleString('ru-RU')}} ₽</span></p>
                                </v-col>
                            </v-row>
                        </v-card-text>

                        <div v-if="!auth">
                            <v-card-actions>
                                <!--В DIALOG!!!-->
                                <v-btn class="cp" tile block outlined color="success" @click="toLogin()">
                                    <v-icon left>mdi-login-variant</v-icon>
                                    Войдите, что бы получить скидку!
                                </v-btn>
                            </v-card-actions>
                        </div>

                        <div v-else>
                            <v-card-text>
                                <v-row>
                                    <v-col>
                                        Бонусов за заказ: <span class="cp"><strong>{{totalBonus}}</strong></span>
                                    </v-col>
                                </v-row>
                                <v-row>
                                    <v-col>
                                        Ваши бонусные рубли: <span><strong>{{bonusAvailable}}</strong></span>
                                    </v-col>
                                </v-row>
                            </v-card-text>
                        </div>

                        <v-divider></v-divider>

                        <div>
                            <v-row>
                                <v-col cols="5">
                                    <v-card-text>
                                        Всего наименований
                                    </v-card-text>
                                </v-col>
                                <v-col>
                                    <v-badge left color="#000000">
                                        <template v-slot:badge>
                                            <span>{{productAmount}}</span>
                                        </template>
                                    </v-badge>
                                </v-col>
                            </v-row>

                            <v-row>
                                <v-col cols="5">
                                    <v-card-text>
                                        Количество едениц
                                    </v-card-text>
                                </v-col>

                                <v-col>
                                    <v-badge left color="#000000">
                                        <template v-slot:badge>
                                            <span>{{itemAmount}}</span>
                                        </template>
                                    </v-badge>
                                </v-col>
                            </v-row>

                        </div>

                        <v-divider></v-divider>

                        <v-card-actions v-if="productAmount !== 0">
                            <v-dialog v-model="orderDialog" persistent max-width="1000">

                                <template v-slot:activator="{ on }">
                                    <v-btn block color="#e52d00" dark v-on="on">Оформить заказ</v-btn>
                                </template>

                                <v-card outlined>
                                    <v-card-title class="headline">
                                        <span v-if="auth" class="mr-1">{{firstName}},</span><span v-else class="mr-1">Ваш </span> заказ на сумму
                                        <span style="color: #e52d00" class="ml-2" v-if="!discountApplied">{{totalPrice.toLocaleString('ru-RU')}} ₽</span>
                                        <span v-else style="color: green" class="ml-2">{{discountPrice.toLocaleString('ru-RU')}} ₽ со скидкой</span>
                                    </v-card-title>

                                    <v-divider></v-divider>

                                    <div v-if="auth">
                                        <v-card-text >
                                            <v-row>
                                                <v-col cols="3">
                                                    Ваши бонусные рубли: <span><strong>{{bonusAvailable}}</strong></span>
                                                </v-col>
                                                <v-col cols="3">
                                                    Ваша скидка: <span><strong>{{discountPercent}}%</strong></span>
                                                </v-col>
                                                <v-col cols="3" v-if="!discountApplied && bonusAvailable !== 0">
                                                    <v-btn class="cp" tile outlined color="primary" @click="applyDiscount()">
                                                        <v-icon left>mdi-sale</v-icon>
                                                        Применить скидку
                                                    </v-btn>
                                                </v-col>
                                            </v-row>
                                        </v-card-text>
                                        <v-divider></v-divider>
                                    </div>
                                    <v-card-text>
                                        <div class="mt-3" >
                                            <h5>Ваши контактные данные</h5>
                                            <v-row>
                                                <v-col>
                                                    <v-text-field prepend-icon="mdi-account" type="text"
                                                                  v-model="lastName"
                                                                  label="Фамилия"
                                                                  :error-messages="lastNameErrors"
                                                                  required
                                                                  @input="$v.lastName.$touch()"
                                                                  @blur="$v.lastName.$touch()"
                                                    ></v-text-field>
                                                </v-col>
                                                <v-col>
                                                    <v-text-field type="text"
                                                                  v-model="firstName"
                                                                  label="Имя"
                                                                  :error-messages="firstNameErrors"
                                                                  required
                                                                  @input="$v.firstName.$touch()"
                                                                  @blur="$v.firstName.$touch()"
                                                    ></v-text-field>
                                                </v-col>
                                                <v-col>
                                                    <v-text-field type="text"
                                                                  v-model="patronymic"
                                                                  label="Отчество"
                                                                  :error-messages="patronymicErrors"
                                                                  required
                                                                  @input="$v.patronymic.$touch()"
                                                                  @blur="$v.patronymic.$touch()"
                                                    ></v-text-field>
                                                </v-col>
                                            </v-row>
                                            <v-row>
                                                <v-col>
                                                    <v-text-field type="text"
                                                                  prepend-icon="mdi-phone"
                                                                  v-mask="'7-###-###-##-##'"
                                                                  v-model="mobile"
                                                                  label="Номер телефона"
                                                                  :error-messages="mobileErrors"
                                                                  required
                                                                  @input="$v.firstName.$touch()"
                                                                  @blur="$v.firstName.$touch()"
                                                    ></v-text-field>
                                                </v-col>
                                                <v-col>
                                                    <v-text-field type="email" prepend-icon="mdi-email" v-model="email" label="E-mail"></v-text-field>
                                                </v-col>
                                            </v-row>
                                        </div>
                                    </v-card-text>
                                    <v-divider></v-divider>
                                    <v-card-text>
                                        <div class="mt-3">
                                            <h5>Способ получения товара</h5>
                                            <v-tabs>
                                                <v-tab @click="noDelivery = true">Самовывоз</v-tab>
                                                <v-tab @click="noDelivery = false">Доставка</v-tab>
                                            </v-tabs>
                                            <div class="mt-3" v-if="noDelivery">
                                                <span>Заберите ваш заказ с магазина по адресу: <strong>город Чебаркуль, Карпенко 7</strong></span>
                                            </div>
                                            <div class="mt-3" v-else>
                                                <v-row>
                                                    <v-col>
                                                        <v-text-field type="text"
                                                                      v-model="city"
                                                                      label="Населенный пункт"
                                                                      :error-messages="cityErrors"
                                                                      required
                                                                      @input="$v.city.$touch()"
                                                                      @blur="$v.city.$touch()"
                                                        ></v-text-field>
                                                    </v-col>
                                                    <v-col>
                                                        <v-text-field type="text"
                                                                      v-model="street"
                                                                      label="Улица"
                                                                      :error-messages="streetErrors"
                                                                      required
                                                                      @input="$v.street.$touch()"
                                                                      @blur="$v.street.$touch()"
                                                        ></v-text-field>
                                                    </v-col>
                                                    <v-col>
                                                        <v-text-field type="text"
                                                                      v-model="house"
                                                                      label="Дом"
                                                                      :error-messages="houseErrors"
                                                                      required
                                                                      @input="$v.house.$touch()"
                                                                      @blur="$v.house.$touch()"
                                                        ></v-text-field>
                                                    </v-col>
                                                    <v-col>
                                                        <v-text-field type="text" v-model="flat" label="Квартира"></v-text-field>
                                                    </v-col>
                                                </v-row>
                                            </div>
                                        </div>
                                    </v-card-text>
                                    <v-card-actions class="chartAreaWrapper">
                                        <v-row>
                                            <v-col>
                                                <v-btn color="green" block dark @click="acceptOrder()">
                                                    Оформить
                                                    <v-icon dark right>mdi-checkbox-marked-circle</v-icon>
                                                </v-btn>
                                            </v-col>
                                            <v-col>
                                                <v-btn color="#e10c0c" block dark @click="cancelOrder()">
                                                    Отмена
                                                    <v-icon dark right>mdi-cancel</v-icon>
                                                </v-btn>
                                            </v-col>
                                        </v-row>
                                    </v-card-actions>
                                </v-card>
                            </v-dialog>
                        </v-card-actions>
                    </v-card>


                        <v-row align="center" justify="center" v-else>
                            <v-card width="300">
                                <v-card-title>
                                    Корзина пуста
                                </v-card-title>
                                <v-card-actions class="chartAreaWrapper">
                                    <v-btn color="#e52d00" block dark @click="toCatalog()">
                                        В каталог
                                    </v-btn>
                                </v-card-actions>
                            </v-card>
                        </v-row>



                    <!--<v-card v-else width="300">
                        <v-card-title>
                            Корзина пуста
                        </v-card-title>
                        <v-card-actions class="chartAreaWrapper">
                            <v-btn color="#e52d00" block dark @click="toCatalog()">
                                В каталог
                            </v-btn>
                        </v-card-actions>
                    </v-card>-->
                </v-col>
            </v-row>
        </b-container>
    </v-content>
</template>

<script>
    import axios from 'axios'
    import { validationMixin } from 'vuelidate'
    import { required, minLength, email } from 'vuelidate/lib/validators'
    export default {
        mixins: [validationMixin],
        validations: {
            lastName:   { required, minLength: minLength(2) },
            firstName:  { required, minLength: minLength(2) },
            patronymic: { required, minLength: minLength(2) },
            mobile:     { required, minLength: minLength(15) },
            city:       { required, minLength: minLength(2) },
            street:     { required, minLength: minLength(2) },
            house:      { required, }
        },

        data: () => ({
            orderedProducts : {},
            totalPrice      : 0,
            totalBonus      : 0,
            productAmount   : 0,
            itemAmount      : 0,
            loading         : true,
            orderDialog     : false,
            noDelivery      : true,
            lastName        : '',
            firstName       : '',
            patronymic      : '',
            mobile          : '',
            email           : '',
            city            :'',
            street          :'',
            house           :'',
            flat            :'',
            discountPercent :0,
            discountAmount  :0,
            discountApplied :false,


        }),
        created() {
            axios.post('/api/order/orderedProducts').then(response => {
                this.loadData(response)
            })
            if (this.auth) {
                this.lastName   = this.$store.state.currentUser.lastName
                this.firstName  = this.$store.state.currentUser.firstName
                this.patronymic = this.$store.state.currentUser.patronymic
                this.mobile     = this.$store.state.currentUser.username
                this.email      = this.$store.state.currentUser.email
            }
            axios.get('/api/order/showUserBonus').then((response) => {
                this.$store.dispatch('setUserBonus', response.data)
            })
        },
        methods: {
            loadData(response) {
                const order = response.data[0]
                const productList = response.data[1]
                this.$store.dispatch('updateOrder', order)

                let orderedProducts = new Map()
                let itemAmount = 0
                for (const [product, amount] of Object.entries(productList)) {
                    const obj = JSON.parse(product);
                    orderedProducts.set(obj, amount)
                    itemAmount += amount
                }
                this.itemAmount = itemAmount
                this.productAmount = orderedProducts.size
                this.orderedProducts = orderedProducts
                this.totalPrice = order.totalPrice
                this.totalBonus = order.totalBonus
                this.loading = false
                this.discountPercent = this.calculateDiscount

                /*На всякий случай*/
                if (Object.keys(productList).length === 0) {
                    this.$store.state.currentOrder.totalBonus = 0
                    this.$store.state.currentOrder.totalPrice = 0
                    this.totalPrice = 0
                    this.totalBonus = 0
                }
            },
            deleteProduct(productID) {
                axios.post('/api/order/deleteProduct', productID).then(response => {
                    this.loadData(response)
                    this.$store.dispatch('removeOrderedProduct', productID)
                })
            },
            increaseAmount(productID) {
                axios.post('/api/order/increaseAmount', productID).then(response => {
                    this.loadData(response)
                })
            },
            decreaseAmount(productID) {
                axios.post('/api/order/decreaseAmount', productID).then(response => {
                    this.loadData(response)
                })
            },
            applyDiscount() {
                const discountAmount = Math.round(this.discountAmount)
                const price = Math.round(this.$store.state.currentOrder.totalPrice)

                this.$store.state.currentUser.bonus -= discountAmount
                this.$store.state.currentOrder.user.bonus -= discountAmount
                this.$store.state.currentOrder.discountPrice = price - discountAmount
                this.discountApplied = true
            },
            acceptOrder() {
                this.$v.$touch()

                if (this.customerDataValid) {

                    let address = ''
                    if (this.city) {
                        address = this.city + ';' + this.street  + ';' + this.house  + ';' + this.flat
                    }

                    let orderDetails = {
                        'orderID'   :this.order.orderID,
                        'firstName' :this.firstName,
                        'lastName'  :this.lastName,
                        'patronymic':this.patronymic,
                        'mobile'    :this.mobile,
                        'email'     :this.email,
                        'address'   :address
                    }

                    if (this.discountApplied) {
                        orderDetails.discountAmount = Math.round(this.discountAmount)
                        orderDetails.discountPrice = Math.round(this.$store.state.currentOrder.discountPrice)
                        orderDetails.userID = Math.round(this.$store.state.currentUser.userID)
                    }

                    axios.post('/api/order/acceptOrder', orderDetails).then(() => {
                        this.$store.dispatch('acceptOrder')
                        this.$store.dispatch('clearOrderedProducts')
                        this.orderDialog = false
                        this.$router.push('/user/cabinet')
                    })

                    this.orderDialog = false
                }
            },
            cancelOrder() {
                if (this.discountApplied) {
                    this.$store.state.currentUser.bonus += Math.round(this.discountAmount)
                    this.$store.state.currentOrder.user.bonus += Math.round(this.discountAmount)
                    this.$store.state.currentOrder.discountPrice = null
                    this.discountApplied = false
                }
                this.orderDialog = false
            },
            toCatalog() {
                this.$router.push('/')
            },
            toLogin() {
                this.$router.push('/login')
            }
        },
        computed: {
            lastNameErrors() {
                const errors = []
                if (!this.$v.lastName.$dirty) return errors
                !this.$v.lastName.minLength && errors.push('Не менее 2 символов')
                !this.$v.lastName.required && errors.push('Введите фамилию')
                return errors
            },
            firstNameErrors() {
                const errors = []
                if (!this.$v.firstName.$dirty) return errors
                !this.$v.firstName.minLength && errors.push('Не менее 2 символов')
                !this.$v.firstName.required && errors.push('Введите имя')
                return errors
            },
            patronymicErrors() {
                const errors = []
                if (!this.$v.patronymic.$dirty) return errors
                !this.$v.patronymic.minLength && errors.push('Не менее 2 символов')
                !this.$v.patronymic.required && errors.push('Введите отчество')
                return errors
            },
            mobileErrors() {
                const errors = []
                if (!this.$v.mobile.$dirty) return errors
                !this.$v.mobile.minLength && errors.push('Номер телефона в формате +7-9xx-xxx-xx-xx')
                !this.$v.mobile.required && errors.push('Введите номер телефона')
                return errors
            },
            cityErrors() {
                const errors = []
                if (!this.$v.city.$dirty) return errors
                !this.$v.city.minLength && errors.push('Не менее 2 символов')
                !this.$v.city.required && errors.push('Введите город')
                return errors
            },
            streetErrors() {
                const errors = []
                if (!this.$v.street.$dirty) return errors
                !this.$v.street.minLength && errors.push('Не менее 2 символов')
                !this.$v.street.required && errors.push('Введите улицу')
                return errors
            },
            houseErrors() {
                const errors = []
                if (!this.$v.house.$dirty) return errors
                !this.$v.house.required && errors.push('Введите номер дома')
                return errors
            },

            customerDataValid() {
                if (this.noDelivery) {
                    return this.lastNameErrors.length === 0 && this.firstNameErrors.length === 0 && this.patronymicErrors.length === 0 && this.mobileErrors.length === 0
                }
                return this.lastNameErrors.length === 0 && this.firstNameErrors.length === 0 && this.patronymicErrors.length === 0 && this.mobileErrors.length === 0 &&
                    this.cityErrors.length === 0 && this.streetErrors.length === 0 && this.houseErrors.length === 0
            },
            auth() {
                return this.$store.state.currentUser
            },
            order() {
                return this.$store.state.currentOrder
            },
            calculateDiscount() {
                if (!this.auth || this.$store.state.currentUser.bonus === 0) return 0
                const bonus = this.$store.state.currentUser.bonus
                const price = this.$store.state.currentOrder.totalPrice

                let discountPercent = 100 * bonus / price
                this.discountAmount = bonus

                if (discountPercent >= 20) {
                    this.discountAmount = price / 100 * 20
                    return 20
                }
                if (discountPercent === 0) {
                    return 1
                }
                return Math.floor(discountPercent)
            },
            bonusAvailable() {
                return this.$store.state.currentUser.bonus
            },
            discountPrice() {
                return this.$store.state.currentOrder.discountPrice
            },
            orderHasProducts() {
                return this.$store.state.orderedProducts.length > 0
            }
        }
    }
</script>

<style scoped>
    .cp {
        padding-top: 0 !important;
        margin-top: -1rem;
    }
    .chartAreaWrapper {
        overflow-x: hidden;
    }
</style>