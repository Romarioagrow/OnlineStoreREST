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

                    <!--<v-card-actions class="ml-5">
                        <form enctype="multipart/form-data">
                            <v-row class="mb-3"><input type="file" name="fileBrands"
                                                       v-on:change="brandsFileChange($event.target.files)"/></v-row>
                            <v-row v-if="!updatingBrands">
                                <v-btn color="success" v-on:click="uploadBrandsPrice()">Загрузить бренд-прайс</v-btn>
                            </v-row>
                            <v-row v-else>
                                <v-progress-circular indeterminate color="primary"></v-progress-circular>
                            </v-row>
                        </form>
                    </v-card-actions>-->
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

                console.log(this.files)
                let formData = new FormData();

                for( var i = 0; i < this.files.length; i++ ){
                    const file = this.files[i]
                    formData.append('file[' + i + ']', file)
                }

                axios.post('admin/updateDB', formData, {
                    headers: {
                        'Content-Type': 'multipart/form-data'
                    }
                }).then(() => {
                    this.updatingDB = false
                    this.files = []
                });

            },

            brandsFileChange(fileList) {
                this.fileBrands.append("fileBrands", fileList[0], fileList[0].name);
            },
            uploadBrandsPrice() {


                this.updatingBrands = true

                console.log(this.brandFiles)
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


                /*axios.post('admin/uploadFileBrands', this.fileBrands).then(() => {
                    this.updatingBrands = false
                    this.fileBrands = new FormData()
                });*/



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

