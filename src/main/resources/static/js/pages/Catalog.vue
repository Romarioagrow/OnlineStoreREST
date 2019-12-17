<template>
    <div>
        <v-progress-linear indeterminate color="#e52d00" v-if="loading"></v-progress-linear>
        <b-container fluid class="flu">
            <div v-if="!loading" class="indent">
                <v-tabs
                        v-model="openWindow"
                        center-active
                        show-arrows
                        :centered="true"
                        :icons-and-text="true"
                        slider-color="#e52d00"
                        background-color="#ffffff">
                    <v-tab v-for="(value, key, index) of allCategories" :key="index" :href="`#tab-${index}`" class="noUnderline">
                        {{key}}
                        <v-icon>{{icons[index]}}</v-icon>
                    </v-tab>
                </v-tabs>
                <v-tabs-items v-model="openWindow">
                    <v-tab-item v-for="(value, key, index) of allCategories" :key="index" :value="`tab-${index}`">
                        <v-card flat>
                            <v-item-group>
                                <v-row align="center" justify="space-between" class="bkcl">
                                    <catalog-groups v-for="group in value" :key="group[0]" :group="group"></catalog-groups>
                                </v-row>
                            </v-item-group>
                        </v-card>
                    </v-tab-item>
                </v-tabs-items>
            </div>
        </b-container>
        <!--<FooterDiv></FooterDiv>-->
    </div>
</template>

<script>
    import CatalogGroups from "components/catalog/CatalogGroups.vue"
    import catalogJSON from 'assets/json/catalog.json'
    import FooterDiv from "components/app/Footer.vue";
    export default {
        components: {
            CatalogGroups, FooterDiv
        },
        data() {
            return {
                allCategories: new Map(),
                categories: [],
                loading: true,
                icons:[
                    'mdi-television',
                    'mdi-fridge',
                    'mdi-washing-machine',
                    'mdi-dice-4',
                    'mdi-air-conditioner',
                    'mdi-content-cut',
                    'mdi-camera',
                    'mdi-desktop-classic',
                    'mdi-radio-handheld',
                    'mdi-circular-saw',
                    'mdi-shovel',
                    'mdi-lightbulb',
                    'mdi-water-boiler',
                    'mdi-bike',
                    'mdi-coffee',
                    'mdi-archive'
                ],
                categoryWindows: {
                    'Теле-Видео-Аудио':0,
                    'Кухонная техника':1,
                    'Техника для дома':2,
                    'Встраиваемая техника':3,
                    'Климатическая техника':4,
                    'Приборы персонального ухода':5,
                    'Цифровые устройства':6,
                    'Компьютеры и оргтехника':7,
                    'Автотовары':8,
                    'Строительные инструменты':9,
                    'Подсобное хозяйство':10,
                    'Товары для дома':11,
                    'Отопительное оборудование':12,
                    'Спорт и отдых':13,
                    'Посуда и кухонные принадлежности':14,
                    'Сопутствующие товары':15
                },
                openWindow: 'tab-0',
            }
        },
        created() {
            this.allCategories = catalogJSON
            this.loading = false

            const uri = decodeURI(window.location.pathname)
            let uriCategory = uri.substr(uri.lastIndexOf('/')+1)

            if (uriCategory !== 'catalog') {
                this.openWindow = 'tab-' + this.categoryWindows[uriCategory]
            }
        }
    }
</script>
<style scoped>
    .indent {
        padding-left: 8rem;
        padding-right: 8rem;
    }
    .bkcl {
        background-color: #fafafa;
    }
    .flu {
        width: 95%;
    }
    .noUnderline {
        text-decoration: none;
    }
</style>