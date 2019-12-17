import Vue from 'vue'
import Vuex from 'vuex'
import axios from 'axios'
import createPersistedState from 'vuex-persistedstate'

Vue.use(Vuex)
import router from './router'

export default new Vuex.Store({
    state: {
        currentUser: null,
        currentOrder: null,
        orderedProducts: [],

        filtersClosedButton: false,
        showSearchedArea: false,

        searchQuery: '',
        searchedProducts: [],
        searchLoading: false,

        adminMode: false
    },
    mutations: {
        setSearchQuery(currentState, query) {
            currentState.searchQuery = query
        },
        setSearchedProducts(currentState, searchedProducts) {
            currentState.searchedProducts = searchedProducts
        },
        showSearchedAreaTrue(currentState) {
            currentState.showSearchedArea = true
        },
        hideSearchedAreaTrue(currentState) {
            currentState.showSearchedArea = false
        },

        setCurrentUser(currentState, user) {
            currentState.currentUser = user
        },
        logoutUser(currentState) {
            currentState.currentUser = null
        },

        setOrderDB(currentState, order) {
            currentState.currentOrder = order
        },
        noCurrentOrder(currentState) {
            currentState.currentOrder = null
        },
        pushOrderedProduct(currentState, productID) {
            currentState.orderedProducts.push(productID)
        },
        removeProductFromOrdered(currentState, productID) {
            currentState.orderedProducts.splice(currentState.orderedProducts.indexOf(productID), 1);
        },
        clearOrderedProducts(currentState) {
            currentState.orderedProducts = []
        },

        hideFiltersButton(currentState) {
            currentState.filtersClosedButton = false
        },
        showFiltersButton(currentState) {
            currentState.filtersClosedButton = true
        },

        setUserBonus(currentState, bonus) {
            currentState.currentUser.bonus = bonus
        },

        setAdminModeTrue(currentState) {
            currentState.adminMode = true
        },
        setAdminModeFalse(currentState) {
            currentState.adminMode = false
        }
    },
    actions: {
        searchProducts(context, searchQuery) {
            context.commit('setSearchQuery', searchQuery)

            const request = this.state.searchQuery
            console.log(request)

            if (request) {
                const url = '/api/products/search'
                const headers = {
                    'Content-Type': 'application/json',
                }
                axios.post(url, request, {
                    headers: headers
                })
                    .then((response) => {
                        context.commit('setSearchedProducts', response.data)
                        context.commit('showSearchedAreaTrue')
                    })
                    .catch((error) => {
                        dispatch({
                            type: ERROR_FINDING_USER
                        })
                    })
            }
        },
        login(context) {
            let sessionProducts
            if (this.state.currentOrder != null) {
                sessionProducts = this.state.currentOrder.orderedProducts
            }
            return axios.post('/auth/addSessionProductToUserOrder', sessionProducts).then((response) => {
                const user = response.data[0]
                const order = response.data[1]
                context.commit('setCurrentUser', user)
                context.commit('setOrderDB', order)
                router.push('/order')

                //console.log(user.data[0])
                if (user.admin) {
                    context.commit('setAdminModeTrue')
                }

            }).catch(reason => {
                console.log(reason)
            })
        },
        logout(context) {
            context.commit('logoutUser')
            context.commit('setAdminModeFalse')
        },
        updateOrder(context, order) {
            context.commit('setOrderDB', order)
            for (const [key, value] of Object.entries(order.orderedProducts)) {
                let productID = key.replace('=', '')
                if (!this.state.orderedProducts.includes(productID))
                    context.commit('pushOrderedProduct', key.replace('=', ''))
            }
        },
        acceptOrder(context) {
            context.commit('noCurrentOrder')
        },
        removeOrder(context) {
            context.commit('noCurrentOrder')
        },
        addOrderedProduct(context, productID) {
            context.commit('pushOrderedProduct', productID)
        },
        removeOrderedProduct(context, productID) {
            context.commit('removeProductFromOrdered', productID)
        },
        clearOrderedProducts(context) {
            context.commit('clearOrderedProducts')
        },
        hideFilters(context) {
            context.commit('hideFiltersButton')
        },
        showFiltersButton(context) {
            context.commit('showFiltersButton')
        },
        showSearchedArea(context) {
            context.commit('showSearchedAreaTrue')
        },
        hideSearchedArea(context) {
            context.commit('hideSearchedAreaTrue')
        },
        setUserBonus(context, bonus) {
            context.commit('setUserBonus', bonus)
        },
    },
    plugins: [createPersistedState()]
});