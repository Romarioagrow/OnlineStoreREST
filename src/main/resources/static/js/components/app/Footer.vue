<template>
    <v-footer padless style="margin-top: 5%; z-index: 1000">
        <v-card flat tile color="#f48337" width="100%" class="white--text text-center" style="padding-left: 10%; padding-right: 10%">
            <!--<v-card-text class="white&#45;&#45;text">
                <v-row>
                    <v-col>
                        <div style="text-align: left" class="title">
                            Время работы<br>
                            Пн-Пт: 9:00 - 18:00<br>
                            Cб: 8:00 - 17:00<br>
                            Вс: 8:00 - 16:00<br>
                        </div>
                    </v-col>
                    <v-col>
                        right
                    </v-col>
                </v-row>
            </v-card-text>

            <v-divider></v-divider>-->

            <v-card-text class="white--text">
                <span class="subtitle-1">Город Чебаркуль, Карпенко 7, тел. <strong>+7 (351-68) 2-93-93</strong></span>
            </v-card-text>
        </v-card>
    </v-footer>

</template>

<script>
    export default {
        data() {
            return {
                icons: [
                    'fab fa-facebook',
                    'fab fa-twitter',
                    'fab fa-google-plus',
                    'fab fa-linkedin',
                    'fab fa-instagram',
                ]
            }
        }
    }
</script>

<style scoped></style>