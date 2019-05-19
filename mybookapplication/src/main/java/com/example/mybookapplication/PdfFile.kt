package com.example.mybookapplication

class PdfFile {
    var pdfFileName : String? = null
    var pdfFilePath : String? = null

    constructor(pdfFileName: String, pdfFilePath: String) {
        this.pdfFileName = pdfFileName
        this.pdfFilePath = pdfFilePath
    }
}