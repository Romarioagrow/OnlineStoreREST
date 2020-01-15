<template>
    <v-expansion-panel>
        <v-expansion-panel-header ripple>
            {{ diapasonKey }}
        </v-expansion-panel-header>

        <v-expansion-panel-content eager>
            <v-range-slider
                    color="#e52d00"
                    v-model="range"
                    :min="min"
                    :max="max"
                    hide-details
                    class="align-center"
                    @end="doSlideFilter()"
            >
                <template v-slot:prepend>
                    <v-text-field outlined
                                  dense
                                  color="#e52d00"
                                  @input="doFilter()"
                                  v-model="range[0]"
                                  type="string"
                                  style="width: 60px; margin-top: 1.5rem;">
                    </v-text-field>
                </template>
                <template v-slot:append>
                    <v-text-field outlined
                                  dense
                                  color="#e52d00"
                                  @input="doFilter()"
                                  v-model="range[1]"
                                  type="string"
                                  style="width: 60px; margin-top: 1.5rem;">
                    </v-text-field>
                </template>
            </v-range-slider>

            <v-row v-if="diapasonInFilter" class="pt-0 pb-0">
                <v-col>
                    <v-btn block outlined small @click="removeDiapason()">По умолчанию</v-btn>
                </v-col>
            </v-row>

        </v-expansion-panel-content>
    </v-expansion-panel>
</template>

<script>
    export default {
        props: ['diapasonKey', 'diapasonVal', 'filterProducts', 'filtersMap'],
        data() {
            return {
                min: this.diapasonVal[0],
                max: this.diapasonVal[1],
                range: this.diapasonVal,
                resetRange: this.diapasonVal,
                diapasonInFilter: false
            }
        },
        methods: {
            doFilter() {
                this.diapasonInFilter = true
                const filterParam = 'diapason;' + this.diapasonKey + ':' + this.range[0] + ',' + this.range[1]
                this.filterProducts(filterParam)
            },

            doSlideFilter() {
                this.diapasonInFilter = true
                const filterParam = 'diapason;' + this.diapasonKey + ':' + this.range
                this.filterProducts(filterParam)
            },

            removeDiapason() {
                const resetKey = 'diapason;' + this.diapasonKey
                this.filtersMap.delete(resetKey)
                this.range = this.resetRange
                this.diapasonInFilter = false
                this.filterProducts('reset')
            }
        }
    }
</script>

<style scoped>

</style>