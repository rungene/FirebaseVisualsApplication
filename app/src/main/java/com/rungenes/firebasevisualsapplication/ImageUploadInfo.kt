package com.rungenes.firebasevisualsapplication

class ImageUploadInfo {
    var title: String? = null
    var description: String? = null
    var image: String? = null
    var search: String? = null

    constructor() {}
    constructor(title: String?, description: String?, image: String?, search: String?) {
        this.title = title
        this.description = description
        this.image = image
        this.search = search
    }
}