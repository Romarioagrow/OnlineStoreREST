<template>
    <v-app >
        <navbar></navbar>
        <v-content class="bg">
            <div @click="hideSearch()">
                <router-view ></router-view>
            </div>
        </v-content>
        <FooterDiv></FooterDiv>
    </v-app>
</template>

<script>
    import axios from 'axios'
    import Navbar from "components/app/Navbar.vue";
    import FooterDiv from "components/app/Footer.vue";
    export default {
        components: {Navbar, FooterDiv},
        beforeCreate() {
            axios.get('/api/order/checkSessionOrder').then(response => {
                if (response.data === false) {
                    this.$store.dispatch('removeOrder')
                    this.$store.dispatch('clearOrderedProducts')
                }
            })

            if (this.$store.state.currentUser !== null) {
                axios.post('/auth/checkCorrectLogin').then(response => {
                    if (response.data === true) {
                        this.$store.dispatch('logout')
                    }
                })
            }
        },
        methods: {
            hideSearch() {
                this.$store.dispatch('hideSearchedArea')
            }
        }
    }
</script>

<style scoped>
    .bg{
         background-color: #fafafa;
     }
</style>

