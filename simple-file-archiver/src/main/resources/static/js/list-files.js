$(document).ready(function () {

    if (!$.fn.dataTable.isDataTable('#filesTable')) {
        var table = $('#filesTable').DataTable({
            autoWidth: true,
            searching: true,
            paging: true,
            info: true,
            select: {style: 'single'},
            language: {search: ""},
            lengthChange: true,
            pageLength: 10,
            dom: 'lftip',
            data: tableData,
            columns: [{
                title: "ID",
                data: "id"
            }, {
                title: "Name",
                data: "fileName"
            }, {
                title: "Path",
                data: "savedPath"
            }, {
                title: "Category",
                data: "category.categoryName"
            }, {
                title: "Status",
                data: "isRemoved"
            }, {
                title: "Permissions",
                data: "filePermissions"
            }, {
                title: "Action",
                data: null
            }],
            columnDefs: [{
                "targets": 0,
                "width": "70px"
            }, {
                "targets": -1,
                "data": null,
                "width": "200px",
                "defaultContent": "<col><button id='downloadButton'>Download</button></col>" +
                " <col><button id='deleteButton'>Delete</button></col>"
            }]
        });

        $('#filesTable_filter input[type="search"]').attr('placeholder', 'Search');

        var filesTableBody = $('#filesTable tbody');

        filesTableBody.on('dblclick', 'tr', function () {
            var rowDataFileId = table.row(this).data().id;
            window.location.href = GET_FORM_URL + '?id=' + rowDataFileId;
        });

        filesTableBody.on('click', '#downloadButton', function () {
            var rowDataFileId = table.row($(this).parents('tr')).data().id;
            window.location.href = DOWNLOAD_FILE_URL + '?id=' + rowDataFileId;
        });

        filesTableBody.on('click', '#deleteButton', function () {
            var row = table.row($(this).parents('tr'));
            var rowDataFileId = row.data().id;
            postJSON(DELETE_FILE_URL, {id: rowDataFileId}, function (response) {
                loadAlertFromServer('success', response.result);
                setTimeout(location.reload.bind(location), 3000);
            }, function (xhr, status, errorThrown) {
                loadAlertFromServer('danger', xhr.responseJSON.errorMessage)
            });
        });
    }

    var tableLengthElement = $('#filesTable_length');
    tableLengthElement.empty();
    tableLengthElement.prepend('<form method="get" action="' + GET_FORM_URL
        + '"><button id="uploadButton" class="button button-default">Upload New</button></form>')

});