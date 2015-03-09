var Profile = function () {

	var handleProfile = function () {

		function format(state) {
            if (!state.id) return state.text; // optgroup
            return "<img class='flag' src='/img/flags/" + state.id.toLowerCase() + ".png'/>&nbsp;&nbsp;" + state.text;
        }


		$("#select2_country").select2({
		  	placeholder: '<i class="fa fa-map-marker"></i>&nbsp;Selecciona tu país',
            allowClear: true,
            formatResult: format,
            formatSelection: format,
            escapeMarkup: function (m) {
                return m;
            }
        });


        $('#select2_country').change(function () {
            $('#form_profile').validate().element($(this)); //revalidate the chosen dropdown value and show error or success message for the input
        });

         $('#form_profile').validate({
	            errorElement: 'span', //default input error message container
	            errorClass: 'help-block', // default input error message class
	            focusInvalid: false, // do not focus the last invalid input
	            ignore: "",
	            rules: {

	                fullname: {
	                    required: true
	                },
	                email: {
	                    required: true,
	                    email: true
	                },
	                city: {
	                    required: true
	                },
	                country: {
	                    required: true
	                },

	                username: {
	                    required: true
	                },
	                password: {
	                    required: true
	                },
	                rpassword: {
	                    equalTo: "#register_password"
	                }
	            },

	            messages: {
                    fullname: {
                        required: "Campo obligatorio"
                    },
                    email: {
                        required: "Campo obligatorio",
                        email: "E-mail inválido"
                    },
                    city: {
                        required: "Campo obligatorio"
                    },
                    country: {
                        required: "Campo obligatorio"
                    },
                    username: {
                        required: "Campo obligatorio"
                    },
                    password: {
                        required: "Campo obligatorio"
                    },
                    rpassword: {
                        required: "Campo obligatorio",
                        equalTo: "Password y confirmación no coinciden"
                    }
	            },

	            invalidHandler: function (event, validator) { //display error alert on form submit

	            },

	            highlight: function (element) { // hightlight error inputs
	                $(element)
	                    .closest('.form-group').addClass('has-error'); // set error class to the control group
	            },

	            success: function (label) {
	                label.closest('.form-group').removeClass('has-error');
	                label.remove();
	            },

	            errorPlacement: function (error, element) {
	                if (element.attr("name") == "tnc") { // insert checkbox errors after the container
	                    error.insertAfter($('#register_tnc_error'));
	                } else if (element.closest('.input-icon').size() === 1) {
	                    error.insertAfter(element.closest('.input-icon'));
	                } else {
	                	error.insertAfter(element);
	                }
	            },

	            submitHandler: function (form) {
	                form.submit();
	            }
	        });

			$('#form_profile input').keypress(function (e) {
	            if (e.which == 13) {
	                if ($('#form_profile').validate().form()) {
	                    $('#form_profile').submit();
	                }
	                return false;
	            }
	        });
	}

    var handlePasswords = function () {


             $('#form_update_password').validate({
    	            errorElement: 'span', //default input error message container
    	            errorClass: 'help-block', // default input error message class
    	            focusInvalid: false, // do not focus the last invalid input
    	            ignore: "",
    	            rules: {
    	                newPass: {
    	                    required: true
    	                },
    	                reNewPass: {
    	                    equalTo: "#newPass"
    	                }
    	            },

    	            messages: {
                        newPass: {
                            required: "Campo obligatorio"
                        },
                        reNewPass: {
                            required: "Campo obligatorio",
                            equalTo: "Password y confirmación no coinciden"
                        }
    	            },

    	            invalidHandler: function (event, validator) { //display error alert on form submit

    	            },

    	            highlight: function (element) { // hightlight error inputs
    	                $(element)
    	                    .closest('.form-group').addClass('has-error'); // set error class to the control group
    	            },

    	            success: function (label) {
    	                label.closest('.form-group').removeClass('has-error');
    	                label.remove();
    	            },

    	            errorPlacement: function (error, element) {
    	                if (element.attr("name") == "tnc") { // insert checkbox errors after the container
    	                    error.insertAfter($('#register_tnc_error'));
    	                } else if (element.closest('.input-icon').size() === 1) {
    	                    error.insertAfter(element.closest('.input-icon'));
    	                } else {
    	                	error.insertAfter(element);
    	                }
    	            },

    	            submitHandler: function (form) {
    	                form.submit();
    	            }
    	        });

    			$('#form_update_password input').keypress(function (e) {
    	            if (e.which == 13) {
    	                if ($('#form_update_password').validate().form()) {
    	                    $('#form_update_password').submit();
    	                }
    	                return false;
    	            }
    	        });
    	}

    return {
        //main function to initiate the module
        init: function () {

            handleProfile();
            handlePasswords();
        }

    };

}();