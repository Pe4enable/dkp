/// <reference path="Helpers/apiHelper.js" />

function DealViewModel() {
    this.nodeName ="test";
    this.realty={
        UID: "realtId"
    } ;
    this.bank={
        UID: "bankId"
    } ;
    this.developer={
        UID:"devId",
        INN:"Inn",
        KPP:"kpp",
        OGRN:"ogrn",
        Name:"devName",
        Address:"devAdress"
    };
    this.buyer={
        Passport:"Pass",
        FIO:"fio",
        Birthday:"bith",
        Address:"buyAdress",
        SNILS:"sn"
    };

}

function CreateDeal(){
    var result=bindDealModel;
    alert("sent");
}

var bindDealModel;

$(function () {
    bindDealModel =new DealViewModel();

    apiHelper.me.get().success(function (data) {
        bindDealModel.nodeName=data.me;
        bindDealModel.realty =ko.observable();

        ko.applyBindings(bindDealModel);
    });
    
    
});