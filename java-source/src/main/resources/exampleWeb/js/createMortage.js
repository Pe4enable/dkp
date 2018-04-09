/// <reference path="Helpers/apiHelper.js" />
/// <reference path="Helpers/fileHelper.js" />

function MortageViewModel() {
    var self = this;     
    self.file=  ko.observable();

    self.fileSelect= function (elemet,event) {
        var file =  event.target.files[0];// FileList object

        // Loop through the FileList and render image files as thumbnails.
        //for (var i = 0, f; f = files[i]; i++) {

          // Only process image files.
        //   if (!f.type.match('image.*')) {
        //     continue;
        //   }          

          var reader = new FileReader();

          // Closure to capture the file information.
          reader.onload = (function(theFile) {
            return function(e) {     
                
                switch (event.target.id) {
                    case "tbMortgagePDFDocument":
                        self.PDFDocument.mimetype=file.type;
                        self.PDFDocument.base64=e.target.result;
                        self.PDFDocument.name=file.name;
                        break;
                    case "tbMortgagePDFSIG":
                        self.PDFSIG.mimetype=file.type;
                        self.PDFSIG.base64=e.target.result;
                        self.PDFSIG.name=e.file.name;
                        break;
                    case "tbMortgageXMLDocument":
                        self.XMLDocument.mimetype=file.type;
                        self.XMLDocument.base64=e.target.result;
                        self.XMLDocument.name=file.name;
                        break;
                    case "tbMortgageXMLSIG":
                        self.XMLSIG.mimetype=file.type;
                        self.XMLSIG.base64=e.target.result;
                        self.XMLSIG.name=file.name;
                        break;
                  }
            };                            
        })(file);
        // Read in the image file as a data URL.
        reader.readAsDataURL(file);
      }

    nodeName="";
    this.PDFDocument ={
        name:"",
        mimetype:"",
        base64:""    
    };
    this.PDFDocument.fileSelect= function (elemet,event) {
        var file =  event.target.files[0];// FileList object
          var reader = new FileReader();
          reader.onload = (function(theFile) {
            return function(e) {     
                        self.PDFDocument.mimetype=file.type;
                        self.PDFDocument.base64=e.target.result;
                        self.PDFDocument.name=file.name;
            };                            
        })(file);
        // Read in the image file as a data URL.
        reader.readAsDataURL(file);
      }
    this.PDFSIG={
        name:"",
        mimetype:"",
        base64:""
    };
    this.PDFSIG.fileSelect= function (elemet,event) {
        var file =  event.target.files[0];// FileList object
          var reader = new FileReader();
          reader.onload = (function(theFile) {
            return function(e) {     
                self.PDFSIG.mimetype=file.type;
                self.PDFSIG.base64=e.target.result;
                self.PDFSIG.name=file.name;
            };                            
        })(file);
        // Read in the image file as a data URL.
        reader.readAsDataURL(file);
      }
    this.XMLDocument={
        name:"",
        mimetype:"",
        base64:""
    };
    this.XMLDocument.fileSelect= function (elemet,event) {
        var file =  event.target.files[0];// FileList object
          var reader = new FileReader();
          reader.onload = (function(theFile) {
            return function(e) {     
                self.XMLDocument.mimetype=file.type;
                self.XMLDocument.base64=e.target.result;
                self.XMLDocument.name=file.name;
            };                            
        })(file);
        // Read in the image file as a data URL.
        reader.readAsDataURL(file);
      };
    this.XMLSIG={
        name:"",
        mimetype:"",
        base64:""
    };
    this.XMLSIG.fileSelect= function (elemet,event) {
        var file =  event.target.files[0];// FileList object
          var reader = new FileReader();
          reader.onload = (function(theFile) {
            return function(e) {     
                self.XMLSIG.mimetype=file.type;
                self.XMLSIG.base64=e.target.result;
                self.XMLSIG.name=file.name;
            };                            
        })(file);
        // Read in the image file as a data URL.
        reader.readAsDataURL(file);
      }
}

function CreateMortage(){
    var result=bindMortageModel;

    alert("sent");
}

function fileUpload(data, e)
{
    var file    = e.target.files[0];
    var reader  = new FileReader();

    reader.onloadend = function (onloadend_e) 
    {
        result.PDFDocument.base64 = reader.result; // Here is your base 64 encoded file. Do with it what you want.
        result.PDFDocument.mimetype=reader.type;
    };

    if(file)
    {
        reader.readAsDataURL(file);
    }
};

var bindMortageModel;

$(function () {
    bindMortageModel =new MortageViewModel();

    apiHelper.me.get().success(function (data) {
        bindMortageModel.nodeName=data.me;
        bindMortageModel.PDFDocument.file =ko.observable();

        ko.applyBindings(bindMortageModel);
    });
    
    
});