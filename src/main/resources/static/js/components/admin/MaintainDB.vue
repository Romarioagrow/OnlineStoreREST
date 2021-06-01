<template>
    <v-container>
        <v-row>
            <v-col>
                <v-card>
                    <v-card-title>Обновить БД</v-card-title>

                    <v-card-actions class="ml-5">
                        <v-file-input
                                v-model="files"
                                color="deep-purple accent-4"
                                counter
                                label="XLSX файлы"
                                multiple
                                placeholder="Выберите XLSX файлы"
                                prepend-icon="mdi-paperclip"
                                outlined
                                :show-size="1000"
                        >
                            <template v-slot:selection="{ index, text }">
                                <v-chip
                                        v-if="index < 2"
                                        color="deep-purple accent-4"
                                        dark
                                        label
                                        small
                                >
                                    {{ text }}
                                </v-chip>
                                <span v-else-if="index === 2" class="overline grey--text text--darken-3 mx-2">
                                    +{{ files.length - 2 }} File(s)
                                </span>
                            </template>
                        </v-file-input>
                    </v-card-actions>

                    <v-card-actions>
                        <v-btn v-if="!updatingDB" color="success" v-on:click="uploadExcelFile()">Загрузить</v-btn>
                        <v-progress-circular v-else indeterminate color="primary"></v-progress-circular>
                    </v-card-actions>
                </v-card>
            </v-col>

            <v-col>
                <v-card>
                    <v-card-actions>
                        <v-btn color="red" v-on:click="resetDB()">Сброс базы</v-btn>
                    </v-card-actions>
                </v-card>
            </v-col>
        </v-row>

        <v-row>
            <v-col>
                <v-card>
                    <v-card-title>Бренды Эксперт</v-card-title>

                    <v-card-actions class="ml-5">
                        <v-file-input
                                v-model="brandFiles"
                                color="deep-purple accent-4"
                                counter
                                label="CSV файлы"
                                multiple
                                placeholder="Выберите CSV файлы"
                                prepend-icon="mdi-paperclip"
                                outlined
                                :show-size="1000"
                        >
                            <template v-slot:selection="{ index, text }">
                                <v-chip
                                        v-if="index < 2"
                                        color="deep-purple accent-4"
                                        dark
                                        label
                                        small
                                >
                                    {{ text }}
                                </v-chip>
                                <span v-else-if="index === 2" class="overline grey--text text--darken-3 mx-2">
                                    +{{ brandFiles.length - 2 }} File(s)
                                </span>
                            </template>
                        </v-file-input>
                    </v-card-actions>

                    <v-card-actions>
                        <v-btn v-if="!updatingBrands" color="success" v-on:click="uploadBrandsPrice()">Загрузить</v-btn>
                        <v-progress-circular v-else indeterminate color="primary"></v-progress-circular>
                    </v-card-actions>
                </v-card>
            </v-col>
        </v-row>

        <v-row>
            <v-col>
                <v-card class="mt-3">
                    <v-card-title>Спарсить ссылки фото RUSBT</v-card-title>
                    <v-card-actions class="ml-5">
                        <v-btn color="yellow" v-on:click="parsRUSBTPics()">Запустить парсер</v-btn>
                    </v-card-actions>
                </v-card>
            </v-col>
        </v-row>

        <v-row>
            <v-col>
                <v-card class="mt-3">
                    <v-card-title>Test</v-card-title>
                    <v-card-actions class="ml-5">
                        <v-btn color="yellow" v-on:click="test()">Test</v-btn>
                    </v-card-actions>
                </v-card>
            </v-col>
        </v-row>
    </v-container>
</template>

<script>
    import axios from "axios";

    export default {
        data() {
            return {
                files: [],
                brandFiles: [],
                file: new FormData(),
                fileBrands: new FormData(),
                updatingDB: false,
                updatingBrands: false,
            }
        },
        methods: {
            fileChange(fileList) {
                this.file.append("file", fileList[0], fileList[0].name);
            },
            uploadExcelFile() {
                let formData = new FormData();

                for( var i = 0; i < this.files.length; i++ ){
                    const file = this.files[i]
                    formData.append('file[' + i + ']', file)
                }
                axios.post('admin/updateDB', formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }).then((response) => {
                    console.log('Total products: ' + response)

                    this.updatingDB = false
                    this.files = []

                    axios.post('admin/parseRUSBT').then((response) => {
                        console.log(response.data)
                    });

                });
            },
            brandsFileChange(fileList) {
                this.fileBrands.append("fileBrands", fileList[0], fileList[0].name);
            },
            uploadBrandsPrice() {
                this.updatingBrands = true
                let formData = new FormData();

                for( var i = 0; i < this.brandFiles.length; i++ ){
                    const file = this.brandFiles[i]
                    formData.append('file[' + i + ']', file)
                }

                axios.post('admin/uploadFileBrands', formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }).then(() => {
                    this.updatingBrands = false
                    this.brandFiles = []
                });
            },
            parsRUSBTPics() {
                axios.post('admin/parsePicsRUSBT').then(console.log('pics parsed'));
            },

            resetDB() {
                axios.post('admin/resetDB').then(console.log('DB reset'));
            },

            test() {
                axios.post('admin/test').then(console.log('test ok'));
            }
        }
    }
</script>

