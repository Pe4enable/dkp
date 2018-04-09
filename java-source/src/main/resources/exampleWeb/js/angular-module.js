"use strict";

// --------
// WARNING:
// --------

// THIS CODE IS ONLY MADE AVAILABLE FOR DEMONSTRATION PURPOSES AND IS NOT SECURE!
// DO NOT USE IN PRODUCTION!

// FOR SECURITY REASONS, USING A JAVASCRIPT WEB APP HOSTED VIA THE CORDA NODE IS
// NOT THE RECOMMENDED WAY TO INTERFACE WITH CORDA NODES! HOWEVER, FOR THIS
// PRE-ALPHA RELEASE IT'S A USEFUL WAY TO EXPERIMENT WITH THE PLATFORM AS IT ALLOWS
// YOU TO QUICKLY BUILD A UI FOR DEMONSTRATION PURPOSES.

// GOING FORWARD WE RECOMMEND IMPLEMENTING A STANDALONE WEB SERVER THAT AUTHORISES
// VIA THE NODE'S RPC INTERFACE. IN THE COMING WEEKS WE'LL WRITE A TUTORIAL ON
// HOW BEST TO DO THIS.

const app = angular.module('demoAppModule', ['ui.bootstrap']);

// Fix for unhandled rejections bug.
app.config(['$qProvider', function ($qProvider) {
    $qProvider.errorOnUnhandledRejections(false);
}]);

app.controller('DemoAppController', function($http, $location, $uibModal) {
    const demoApp = this;

    // We identify the node.
    const apiBaseURL = "/api/example/";
    let peers = [];

    $http.get(apiBaseURL + "me").then((response) => demoApp.thisNode = response.data.me);

    $http.get(apiBaseURL + "peers").then((response) => peers = response.data.peers);

    demoApp.openModal = () => {
        const modalInstance = $uibModal.open({
            templateUrl: 'demoAppModal.html',
            controller: 'ModalInstanceCtrl',
            controllerAs: 'modalInstance',
            resolve: {
                demoApp: () => demoApp,
                apiBaseURL: () => apiBaseURL,
                peers: () => peers
            }
        });

        modalInstance.result.then(() => {}, () => {});
    };

    demoApp.getIOUs = () => $http.get(apiBaseURL + "ious")
        .then((response) => demoApp.ious = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());

    demoApp.getIOUs();

    demoApp.getBuilds = () => $http.get(apiBaseURL + "builds")
        .then((response) => demoApp.builds = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());

    demoApp.getBuilds();

    demoApp.getDeals = () => $http.get(apiBaseURL+"deals")
        .then((response) => demoApp.deals = Object.keys(response.data)
            .map((key) => response.data[key].state.data)
            .reverse());

    demoApp.getDeals();

    demoApp.createBuild = () => {
        const counterparty="O=AHMLClient, L=New York, C=US";
        const ahmlParty="O=AHML, L=London, C=GB";
        const createBuildEndpoint = `${apiBaseURL}create-build?ahmlName=${ahmlParty}&clientName=${counterparty}&address=${demoApp.build.address}&price=${demoApp.build.price}&description=${demoApp.build.description}&area=${demoApp.build.area}&comment=${demoApp.build.comment}`;
        
        $http.put(createBuildEndpoint).then(
            (result) => {
                modalInstance.displayMessage(result);
                demoApp.getBuilds();
            },
            (result) => {
                modalInstance.displayMessage(result);
            }
        );
    } 

    demoApp.bookBuild = (buildId) =>{
        const counterparty="O=DEVELOPER, L=Paris, C=FR";
        const bookBuildEndpoint = `${apiBaseURL}book-build?buildId=${buildId.id}`;

        $http.put(bookBuildEndpoint).then(
            (result) => {
                modalInstance.displayMessage(result);
                demoApp.getBuilds();
            },
            (result) => {
                modalInstance.displayMessage(result);
            }
        )
    }

    demoApp.createDeal = (buildId) =>{
        const counterparty="O=DEVELOPER, L=Paris, C=FR";
        const ahmlParty="O=AHML, L=London, C=GB";
        const createDealEndpoint = `${apiBaseURL}create-deal?buildId=${buildId.id}&ahml=${ahmlParty}&developer=${counterparty}`;

        $http.put(createDealEndpoint).then(
            (result) => {
                modalInstance.displayMessage(result);
                demoApp.getBuilds();
            },
            (result) => {
                modalInstance.displayMessage(result);
            }
        )
    }
});

app.controller('ModalInstanceCtrl', function ($http, $location, $uibModalInstance, $uibModal, demoApp, apiBaseURL, peers) {
    const modalInstance = this;

    modalInstance.peers = peers;
    modalInstance.form = {};
    modalInstance.formError = false;

    // Validate and create IOU.
    modalInstance.create = () => {
        if (invalidFormInput()) {
            modalInstance.formError = true;
        } else {
            modalInstance.formError = false;

            $uibModalInstance.close();

            const createIOUEndpoint = `${apiBaseURL}create-iou?partyName=${modalInstance.form.counterparty}&iouValue=${modalInstance.form.value}`;

            // Create PO and handle success / fail responses.
            $http.put(createIOUEndpoint).then(
                (result) => {
                    modalInstance.displayMessage(result);
                    demoApp.getIOUs();
                },
                (result) => {
                    modalInstance.displayMessage(result);
                }
            );
        }
    };

    modalInstance.displayMessage = (message) => {
        const modalInstanceTwo = $uibModal.open({
            templateUrl: 'messageContent.html',
            controller: 'messageCtrl',
            controllerAs: 'modalInstanceTwo',
            resolve: { message: () => message }
        });

        // No behaviour on close / dismiss.
        modalInstanceTwo.result.then(() => {}, () => {});
    };

    // Close create IOU modal dialogue.
    modalInstance.cancel = () => $uibModalInstance.dismiss();

    // Validate the IOU.
    function invalidFormInput() {
        return isNaN(modalInstance.form.value) || (modalInstance.form.counterparty === undefined);
    }
});

// Controller for success/fail modal dialogue.
app.controller('messageCtrl', function ($uibModalInstance, message) {
    const modalInstanceTwo = this;
    modalInstanceTwo.message = message.data;
});