<template>
    <div v-if="created">
        <v-navigation-drawer absolute permanent>
            <template v-slot:prepend>
                <v-list-item two-line>
                    <v-list-item-content>
                        <v-list-item-title>Admin</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
            </template>

            <v-list dense>
                <!--База данных-->
                <v-list-item @click="openDB()">
                    <v-list-item-icon>
                        <v-icon>mdi-file-excel</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content>
                        <v-list-item-title>База данных</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <!--Текущие заказы-->
                <v-list-item @click="openOrders()">
                    <v-list-item-icon>
                        <v-icon>mdi-format-list-bulleted-square</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content >
                        <v-list-item-title>Текущие аказы</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>

                <!--Завершенные заказы-->
                <v-list-item @click="openCompletedOrders()">
                    <v-list-item-icon>
                        <v-icon>mdi-check</v-icon>
                    </v-list-item-icon>
                    <v-list-item-content >
                        <v-list-item-title>Завершенные заказы</v-list-item-title>
                    </v-list-item-content>
                </v-list-item>
            </v-list>

            <v-divider></v-divider>

            <!--Logout-->
            <v-card-actions>
                <v-btn block tile color="red" @click="logout">
                    <span>Выход</span>
                    <v-icon>mdi-logout</v-icon>
                </v-btn>
            </v-card-actions>

        </v-navigation-drawer>

        <v-content>
            <v-container fluid fill-height>

                <!--Работа с базой данных-->
                <MaintainDB v-if="activeMaintainDBContainer"></MaintainDB>

                <!--Принятые заказы-->
                <AcceptedOrders v-if="activeAcceptedOrdersContainer"></AcceptedOrders>

                <!--Завершенные заказы-->
                <CompletedOrders v-if="activeCompletedOrdersContainer"></CompletedOrders>

            </v-container>
        </v-content>
    </div>
</template>

<script>
    import axios from "axios";
    import MaintainDB from "components/admin/MaintainDB.vue";
    import AcceptedOrders from "components/admin/AcceptedOrders.vue";
    import CompletedOrders from "components/admin/CompletedOrders.vue";
    export default {
        components: {
            MaintainDB,
            AcceptedOrders,
            CompletedOrders
        },
        data() {
            return {
                created: false,
                activeMaintainDBContainer: true,
                activeAcceptedOrdersContainer: false,
                activeCompletedOrdersContainer: false,
            }
        },
        beforeCreate() {
            axios.get('/admin').then(() => {
                this.created = true
            }).catch(error =>
            {
                if (error.response) {
                    console.log(error.response.status);
                    if (error.response.status === 403) {
                        this.$router.push('/')
                    }
                }
            })
        },
        methods: {
            logout() {
                axios.post('user/logout').then(() => {
                    this.$store.dispatch('logout')
                    this.$router.push('/')
                })
            },
            openDB() {
                this.activeMaintainDBContainer = true
                this.activeAcceptedOrdersContainer = false
                this.activeCompletedOrdersContainer = false
            },
            openOrders() {
                this.activeMaintainDBContainer = false
                this.activeAcceptedOrdersContainer = true
                this.activeCompletedOrdersContainer = false
            },
            openCompletedOrders() {
                this.activeCompletedOrdersContainer = true
                this.activeMaintainDBContainer = false
                this.activeAcceptedOrdersContainer = false
            }
        }
    }
</script>
