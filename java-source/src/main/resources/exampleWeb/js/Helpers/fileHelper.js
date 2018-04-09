var fileHelper = function () {
    function _getBase64(file) {
        var reader = new FileReader();
        reader.readAsDataURL(file);
        reader.onload = function () {
            return reader.result;
          console.log(reader.result);
        };
        reader.onerror = function (error) {
          console.log('Error: ', error);
        };
     }

    return {
        getBase64:_getBase64
    };
}();