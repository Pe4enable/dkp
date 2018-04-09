/// <reference path="Helpers/apiHelper.js" />
"use strict";

function BuildViewModel() {
    this.nodeName ="test";
    this.Build={
        Address: "",
        Price:"",
        Description:"",
        Area:"",
        Comment:"",
        ahmlBuild:""
    } ;
}

function CreateBuild(){
    var result=bindBuildModel;
    var data={
         address:$("#tbAddress").val(),
         price:$("#tbPrice").val(),
         description:$("#tbDescription").val(),
         area:$("#tbArea").val(),
         comment:$("#tbComment").val(),
         ahmlBuild:"O=PartyB, L=New York, C=US"
    }

    apiHelper.builds.put(data).success(function(result){
        alert("sent");
    });
}

var bindBuildModel;

$(function () {
    bindBuildModel =new BuildViewModel();

    apiHelper.me.get().success(function (data) {
        bindBuildModel.nodeName=data.me;
        bindBuildModel.Build =ko.observable();

        ko.applyBindings(bindBuildModel);
    });
    
    
});