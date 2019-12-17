<template>
    <v-container>
        <v-row>
            <v-col>
                <v-card>
                    <v-card-title>Обновить БД</v-card-title>
                    <v-card-actions class="ml-5">
                        <form enctype="multipart/form-data">
                            <v-row class="mb-3"><input type="file" name="file"
                                                       v-on:change="fileChange($event.target.files)"/></v-row>
                            <v-row v-if="!updatingDB">
                                <v-btn color="success" v-on:click="uploadExcelFile()">Загрузить</v-btn>
                            </v-row>
                            <v-row v-else>
                                <v-progress-circular indeterminate color="primary"></v-progress-circular>
                            </v-row>
                        </form>
                    </v-card-actions>
                </v-card>
            </v-col>
            <v-col>
                <v-card>
                    <v-card-title>Бренды Эксперт</v-card-title>
                    <v-card-actions class="ml-5">
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
    </v-container>
</template>

<script>
    import axios from "axios";

    export default {
        data() {
            return {
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
                this.updatingDB = true
                axios.post('admin/uploadFileDB', this.file).then(() => {
                    this.updatingDB = false
                    this.file = new FormData()
                });

            },
            brandsFileChange(fileList) {
                this.fileBrands.append("fileBrands", fileList[0], fileList[0].name);
            },
            uploadBrandsPrice() {
                this.updatingBrands = true
                axios.post('admin/uploadFileBrands', this.fileBrands).then(() => {
                    this.updatingBrands = false
                    this.fileBrands = new FormData()
                });
            },
            parsRUSBTPics() {
                axios.post('admin/parsePicsRUSBT').then(console.log('pics parsed'));
            },
        }
    }
</script>

