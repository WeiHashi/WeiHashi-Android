package cn.devmeteor.weihashi.model

data class Update(
    var update: Boolean,
    var newVersion: String,
    var apkFileUrl: String,
    var newMd5: String,
    var updateLog: String,
    var targetSize: String,
    var isConstraint: Boolean
)