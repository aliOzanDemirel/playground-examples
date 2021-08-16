$(document).ready(function () {

    $('#approvalDate').val(formatDateForSpring(new Date()));

    if (!$.fn.dataTable.isDataTable('#categoriesTable')) {

        var table = $('#categoriesTable').DataTable({
            autoWidth: true,
            searching: false,
            paging: false,
            info: false,
            pageLength: 10,
            select: {style: 'single'}
        });

        $('#categoriesTable tbody').off('click').on('click', 'tr', function () {
            var el = $(this);
            if (el.hasClass('selected')) {
                el.removeClass('selected');
            } else {
                table.$('tr.selected').removeClass('selected');
                el.addClass('selected');
            }
        });

        $('#removeCategoryButton').off('click').click(function () {
            var row = table.row('.selected');
            postFormData(DELETE_CATEGORY_URL, {categoryId: row.data()[0]}, function (response) {
                row.remove().draw(false);
            }, function (xhr, status, errorThrown) {
                loadAlertFromServer('danger', xhr.responseJSON.errorMessage)
            });
        });
    }

});
