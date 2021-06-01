<template>
    <v-expansion-panel>
        <v-expansion-panel-header ripple>
            {{ toUpperCase(paramKey) }}
        </v-expansion-panel-header>

        <v-expansion-panel-content eager style="margin-top: 10px">
            <div v-for="(checkbox, i) in paramVal" :key="i" :brand="checkbox">
                <v-checkbox
                        :disabled="checkFilterInCheckList(paramKey + ': ' + checkbox)"
                        color="#e52d00"
                        class="mt-2"
                        @change="filterProducts('param;' + paramKey + ': ' + checkbox)"
                        v-model="selectedParam"
                        :label="toUpperCase(checkbox)"
                        :value="paramKey +': '+ checkbox"
                        height="2">
                </v-checkbox>
            </div>
        </v-expansion-panel-content>
    </v-expansion-panel>
</template>

<script>
    export default {
        props: ['paramKey', 'paramVal', 'filterProducts', 'filtersMap', 'checkFilterInCheckList', 'selectedParams', 'toUpperCase'],
        computed: {
            selectedParam: {
                get() {
                    return this.selectedParams
                },
                set(newValue) {
                    this.$emit('update:value', newValue);
                }
            }
        }
    }
</script>

<style scoped>

</style>