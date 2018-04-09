var apiHelper = function () {
    var _partyAUrl = "http://89.178.218.93:10007/api/example/";
    var _partyBUrl = "http://89.178.218.93:10010/api/example/";
    var _partyCUrl = "http://89.178.218.93:10013/api/example/";

    var _peersUrl = "peers";
    var _meUrl="me";
    var _IOUsUrl="ious";
    var _buildsUrl="builds";

    var _createBuild="create-build";

    var _get = function (url) {
        return $.ajax({
            type: "GET",
            contentType: "application/json",
            url: url
        });
    };

    var _post = function(url, data) {
        return $.ajax({
            type: "POST",
            contentType: "application/json",
            url: url,
            data: JSON.stringify(data)
        });
    };

    var _put = function(url){
        return $.ajax({
            type: "PUT",
            contentType: "application/json",
            url: url
         });
    };

    var _peers =(function() {
        return {
            get: function() {
                return _get(_partyAUrl+_peersUrl).data.peers;
            }
        };
    })();

    var _builds =(function() {
        return {
            get: function() {
                return _get(_partyAUrl+_peersUrl).data.peers;
            },
            put:function(data){
                return _put(_partyAUrl
                            + _createBuild
                            + "?ahmlName="+data.ahmlName
                            + "&description"+data.description
                            + "&price"+data.price
                            + "&address"+data.address
                            + "&comment"+data.comment
                            + "&area"+data.area);
            }
        };
    })();

    var _me =(function() {
        return {
            get: function() {
                return _get(_partyAUrl+_meUrl);
            }
        };
    })();

    var _IOUs =(function() {
        return {
            get: function() {
                return _get(_partyAUrl+_meUrl).data;
            }
        };
    })();

    return {
        partyAUrl: _partyAUrl,
        partyBUrl: _partyBUrl,
        partyCUrl: _partyCUrl,
        peers: _peers,
        IOUs: _IOUs,
        me:_me,
        builds: _builds
    };
}();