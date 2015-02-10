$.fn.invoiceTable = function(options) {

	'use strict';
	return $(this).each(function () {
		var buildDefaultOptions = function () {
				var opts = $.extend({}, $.fn.invoiceTable.defaultOptions);
				opts.editor = opts.editor.clone();
				return opts;
			},
			activeOptions = $.extend(buildDefaultOptions(), options),
			ARROW_LEFT = 37, ARROW_UP = 38, ARROW_RIGHT = 39, ARROW_DOWN = 40, ENTER = 13, ESC = 27, TAB = 9,
			element = $(this),
			editor = activeOptions.editor.css('position', 'absolute').hide().appendTo(element.parent()),
			active,
			invoice = {},
			// display form field in place of cell text
			showEditor = function (select) {
				active = element.find('td.editable:focus');
				if (active.length) {
					editor.val(active.text())
						.removeClass('error')
						.show()
						.offset(active.offset())
						.css(active.css(activeOptions.cloneProperties))
						.width(active.width())
						.height(active.height())
						.focus();
					if (select) {
						editor.select();
					}
				}
			},
			// set the cell text after editing
			setActiveText = function () {
				var text = editor.val(),
					evt = $.Event('change'),
					originalContent;
				if (active.text() === text || editor.hasClass('error')) {
					return true;
				}
				originalContent = active.html();
				active.text(text).trigger(evt, text);
				if (evt.result === false) {
					active.html(originalContent);
				}
				else if (active.hasClass("unitPrice")) {
					active.text(parseFloat(text).toFixed(2));
				}
			},
			// allow arrow key navigation with editable elements
			movement = function (element, keycode) {
				if (keycode === ARROW_RIGHT) {
					return element.next('td.editable');
				} else if (keycode === ARROW_LEFT) {
					return element.prev('td.editable');
				} else if (keycode === ARROW_UP) {
					return element.parent().prev().children().eq(element.index());
				} else if (keycode === ARROW_DOWN) {
					return element.parent().next().children().eq(element.index());
				}
				return [];
			},
			//function to calculate totals, and add json string to cookie
			calculateTotal = function() {

				var dataRows = element.find('tbody tr.calculate'),
					subtotal;

				invoice['lineitems'] = [];
				invoice['subtotal'] = 0;

				dataRows.each(function(index) {

					var arrRow = {},
					row = $(this);

					arrRow['description'] = row.find('.description').text(),
					arrRow['quantity'] = row.find('.quantity').text(),
					arrRow['unitPrice'] = row.find('.unitPrice').text();
					arrRow['rowTotal'] = 0;

					if (!isNaN(parseFloat(arrRow['quantity']) && !isNaN(parseFloat(arrRow['unitPrice'])))) {
						arrRow['rowTotal'] = (arrRow['quantity'] * arrRow['unitPrice']);
						row.find('.rowTotal').text(arrRow['rowTotal'].toFixed(2));
						invoice['subtotal'] += arrRow['rowTotal'];
					}

					invoice['lineitems'].push(arrRow);

				});

				invoice['salestax'] = invoice['subtotal'] * activeOptions.salestax / 100;
				invoice['total'] = (invoice['subtotal'] * activeOptions.salestax / 100) + invoice['subtotal'];

				element.find('.calc-subtotal').text(invoice['subtotal'].toFixed(2));
				element.find('.calc-salesTax').text(invoice['salestax'].toFixed(2));
				element.find('.calc-total').text(invoice['total'].toFixed(2));

				createCookie('myinvoice',JSON.stringify(invoice),30);
			},
			// add a row to the table
			addRow = function() {

				var newRow = $('#row-template').clone(true).removeClass("hide").addClass("calculate");
				newRow.appendTo(element.find('tbody'));
				newRow.children().eq(0).text(newRow.siblings().length);
				return newRow;

			},
			// create a cookie
			createCookie = function(name,value,days) {
				if (days) {
					var date = new Date();
					date.setTime(date.getTime()+(days*24*60*60*1000));
					var expires = "; expires="+date.toGMTString();
				}
				else var expires = "";
				document.cookie = name+"="+value+expires+"; path=/";
			},
			// read the cookie
			readCookie = function(name) {
				var nameEQ = name + "=";
				var ca = document.cookie.split(';');
				for(var i=0;i < ca.length;i++) {
					var c = ca[i];
					while (c.charAt(0)==' ') c = c.substring(1,c.length);
					if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
				}
				return null;
			},
			// erase the cookie (on submit invoice)
			eraseCookie = function(name) {
				createCookie(name,"",-1);
			};

		// set sales tax text in the table
		element.find('.salesTaxText').text('IVA ('+ activeOptions.salestax +'%)');

		// if element is blurred set active text
		editor.blur(function () {
			setActiveText();
			editor.hide();
		}).keydown(function (e) {
			if (e.which === ENTER) {
				setActiveText();
				editor.hide();
				active.focus();
				e.preventDefault();
				e.stopPropagation();
			} else if (e.which === ESC) {
				editor.val(active.text());
				e.preventDefault();
				e.stopPropagation();
				editor.hide();
				active.focus();
			} else if (e.which === TAB) {
				active.focus();
			} else if (this.selectionEnd - this.selectionStart === this.value.length) {
				var possibleMove = movement(active, e.which);
				if (possibleMove.length > 0) {
					possibleMove.focus();
					e.preventDefault();
					e.stopPropagation();
				}
			}
		})
		// if content is pasted
		.on('input paste', function () {
			var evt = $.Event('validate');
			active.trigger(evt, editor.val());
			if (evt.result === false) {
				editor.addClass('error');
			} else {
				editor.removeClass('error');
			}
		});

		// set interaction for editable elements
		element.find('td.editable').on('click keypress dblclick', showEditor)
		.css('cursor', 'pointer')
		.keydown(function (e) {
			var prevent = true,
				possibleMove = movement($(e.target), e.which);
			if (possibleMove.length > 0) {
				possibleMove.focus();
			} else if (e.which === ENTER) {
				showEditor(false);
			} else if (e.which === 17 || e.which === 91 || e.which === 93) {
				showEditor(true);
				prevent = false;
			} else {
				prevent = false;
			}
			if (prevent) {
				e.stopPropagation();
				e.preventDefault();
			}
		});

		// set a tab index for editable elements
		element.find('td.editable').prop('tabindex', 1);

		// add click event for add row button
		element.find('#btn-add').on('click', function(){
			addRow();
		});

		// add click event for delete row button
		element.find('#btn-del').on('click', function(){
			element.find('tbody tr.calculate:last').remove();
			if (element.find('.calculate').length === 0) {
				addRow();
			}
			calculateTotal();
		});

		// add click event for delete row button
		element.find('#btn-del').on('click', function(){
			element.find('tbody tr.calculate:last').remove();
			if (element.find('.calculate').length === 0) {
				addRow();
			}
			calculateTotal();
		});

		// add click event for delete row buttons in rows
		element.find('.btn-rowdel').on('click', function(){
			$(this).closest('tr.calculate').remove();
			if (element.find('.calculate').length === 0) {
				addRow();
			}
			calculateTotal();
		});

		// add click event for submit invoice button
		element.find('#btn-sub').on('click', function(){
			$.ajax({
				type: "POST",
				url: "somescript.php", //change this script to whatever you create :-)
				data: {invoice: JSON.stringify(invoice)},
			})
			.done(function(data) {
				alert('Invoice Submitted!');
				eraseCookie('myinvoice');
				element.find('tr.calculate').remove();
				addRow();
				calculateTotal();
			});
			
		});

		// change event for editable elements
		element.find('td.editable').on('change', function(evt) {
			calculateTotal();
		}).on('validate', function (evt, value) {
			if ($(this).hasClass('numeric')) {
				return !isNaN(parseFloat(value)) && isFinite(value);
			}
		});

		// recalculate relative sizes if window is resized
		$(window).on('resize', function () {
			if (editor.is(':visible')) {
				editor.offset(active.offset())
				.width(active.width())
				.height(active.height());
			}
		});

		// if an invoice exists in a cookie, populate the html
		var existingInvoice = readCookie('myinvoice');
		if (existingInvoice) {
			var invoiceObject = $.parseJSON(existingInvoice);
			$.each(invoiceObject.lineitems,function(index,item){
				var newRow = addRow();
				newRow.find('.description').text(item.description);
				if (!isNaN(item.quantity)) {
					newRow.find('.quantity').text(item.quantity);
				}
				if (!isNaN(item.unitPrice) && item.unitPrice) {
					newRow.find('.unitPrice').text(parseFloat(item.unitPrice).toFixed(2));
				}
			});
			calculateTotal();
		} else {
			addRow();
		}
		

	});

};
// set default options
$.fn.invoiceTable.defaultOptions = {
	cloneProperties: ['padding', 'padding-top', 'padding-bottom', 'padding-left', 'padding-right',
					  'text-align', 'font', 'font-size', 'font-family', 'font-weight',
					  'border', 'border-top', 'border-bottom', 'border-left', 'border-right'],
	editor: $('<input class=\"cell\">'),
	salestax: 20
};

