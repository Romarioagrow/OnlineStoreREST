<template>
    <div>
        <v-app-bar
                color="#24344d"
                dark
                :clipped-left="$vuetify.breakpoint.lgAndUp"
                app
                height="70"
                flat
        >
<!--                src="../../../img/navpic.png"
          -->
            <router-link to="/catalog">
                <v-app-bar-nav-icon></v-app-bar-nav-icon>
            </router-link>

            <v-toolbar-title>
                <router-link to="/">
                  <v-btn outlined >HOME</v-btn>
<!--                    <v-img src="../../..//logo.png" contain height="60"></v-img>-->
                </router-link>
            </v-toolbar-title>

            <v-spacer></v-spacer>

            <v-autocomplete
                    id="searchInput"
                    cache-items
                    class="mx-4"
                    flat
                    hide-no-data
                    hide-details
                    label="Найдите что вам нужно"
                    solo-inverted
                    @keyup="searchProducts()"
            ></v-autocomplete>

            <v-container v-if="searchArea" class="display-result">
                <v-list shaped color="white">
                    <v-list-item-group >
                        <v-list-item v-for="product in searchedProducts" :key="product.productID" @click="goToProductPage(product.productID)" color="black" class="searchedItem">

                            <v-list-item-avatar>
                                <v-img :src="product.pic"></v-img>
                            </v-list-item-avatar>

                            <v-list-item-content>
                                <v-list-item-title v-text="product.fullName" style="color: black"></v-list-item-title>
                            </v-list-item-content>

                            <v-list-item-avatar width="150">
                                <v-list-item-title v-text="product.finalPrice+' ₽'" style="color: black"></v-list-item-title>
                            </v-list-item-avatar>

                        </v-list-item>
                    </v-list-item-group>
                </v-list>
            </v-container>

            <v-spacer></v-spacer>

            <div v-if="isAdminMode" style="color:black">AdminMode</div>

            <router-link to="/order" class="ml-5">
                <v-btn tile outlined>
                    Корзина
                    <v-badge color="#000000" right v-if="totalOrderProductsAmount > 0">
                        <template v-slot:badge>
                            <span>{{totalOrderProductsAmount}}</span>
                        </template>
                        <v-icon>mdi-cart</v-icon>
                    </v-badge>
                </v-btn>

            </router-link>

            <div v-if="!admin">
                <router-link to="/user/cabinet" class="ml-5" v-if="auth">
                    <v-btn tile outlined>
                        <span>Личный кабинет</span>
                        <v-icon>mdi-account</v-icon>
                    </v-btn>
                </router-link>

                <router-link to="/login" class="ml-5" v-else>
                    <v-btn tile outlined>
                        <span>Вход</span>
                        <v-icon>mdi-login-variant</v-icon>
                    </v-btn>
                </router-link>
            </div>

            <router-link to="/admin" class="ml-5" v-if="admin">
                <v-btn tile outlined color="black">
                    <span>Admin</span>
                    <v-icon>mdi-account-badge-horizontal</v-icon>
                </v-btn>
            </router-link>

        </v-app-bar>
    </div>
</template>

<script>
    import axios from "axios";
    export default {
        name: "Navbar",
        data: () => ({
            collapseOnScroll: true,
            loading: false,

        }),
        methods: {
            searchProducts() {
                const searchQuery = document.getElementById('searchInput').value
                this.$store.dispatch('searchProducts', searchQuery)

                /*this.loading = true
                this.loading = false
                setTimeout(() => {
                     /!*console.log(this.search)
                     console.log(this.states)*!/
                     axios.post('api/products/searchProducts')
                     this.loading = false
                 }, 500)*/
            },
            goToProductPage(productID) {
                this.$store.commit('hideSearchedAreaTrue')

                if (this.$route.fullPath.includes('product/')) {
                    this.$router.push('/products/product/'+productID)
                    location.reload()
                }
                else this.$router.push('/products/product/'+productID)
            }
        },
        computed: {
            searchedProducts() {
                return this.$store.state.searchedProducts
            },
            auth () {
                return this.$store.state.currentUser && !this.$store.state.currentUser.roles.includes('ADMIN')
            },
            admin() {
                return this.$store.state.currentUser && this.$store.state.currentUser.roles.includes('ADMIN')
            },
            filtersClosed() {
                return this.$store.state.filtersClosedButton
            },
            searchArea() {
                return this.$store.state.showSearchedArea
            },
            totalOrderProductsAmount() {
                return this.$store.state.orderedProducts.length
            },
            isAdminMode() {
                return this.$store.state.adminMode
            }
        }
    }
</script>

<style scoped>
    .searchedItem:hover {
        background-color: #f2f2f2
    }
    .display-result {
        position: absolute;
        color: black;
        width: 50%;
        left: 30%;
        top: 100%;
        max-height: 400px;
        overflow: auto;
        box-shadow: 0 5px 13px 0 rgba(0,0,0,.2);
        background-color: white !important;
        border: 1px solid #ececec;
        padding: 15px;
        z-index: 50;
    }
</style>
