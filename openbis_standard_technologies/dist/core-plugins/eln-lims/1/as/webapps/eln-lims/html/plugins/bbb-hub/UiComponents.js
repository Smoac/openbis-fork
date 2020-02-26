var UiComponents = new function() {

    this.addDropdown = function(label, options, $container, selectAction) {
        var $dropdown = UiComponents.getDropdown(options);
        $inputGroup = UiComponents.getInputGroup($dropdown, label);
        $inputGroup.css("margin-top", "3px");
        $container.append($inputGroup);
        $dropdown.select2({ width : "100%", theme : "bootstrap", minimumResultsForSearch: 10 });
        if (selectAction) {
           $dropdown.on('select2:select', function (e) {
               selectAction(e.params.data.id);
           });
        }
        $inputGroup.getValue = function() {
           return $dropdown.val();
        }
        return $inputGroup;
    }

    this.getDropdown = function(options, value) {
        var $input = $("<select>", { class : "form-control", type : "text" });
        $input.append($("<option>", { "disabled" : true, "selected" : true, "value" : true }).text(" -- select an option -- "));
        for (option of options) {
            var $option = $("<option>");
            $option.text(option.label);
            $option.attr("value", option.value);
            $input.append($option);
        }
        if (value) {
            $input.val(value);
        }
        return $input;
    }

    this.getInputGroup = function($input, label) {
        var $inputGroup = $("<div>", { class : "input-group" });
        $inputGroup.append($("<span>", { class : "input-group-addon" }).text(label).css({ 'min-width' : '150px' }));
        $inputGroup.append($input);
        return $inputGroup;
    }

    this.getButton = function($html, action, size, icon) {
        var buttonSize = size ? size : 'md';
        var $button = $('<button>', {
            type : 'button',
            class : 'btn btn-default btn-' + buttonSize,
            'aria-label' : 'Left Align'
        });
        if(icon) {
            var $icon = $('<span>', { class : 'glyphicon ' + icon });
            $icon.css("margin-right", "0.5rem");
            $button.append($icon);
        }
        if($html) {
            $button.append($("<span>").text($html));
        }
        if(action) {
            $button.click(action);
        }
        return $button;
    }

    this.getFieldset = function(legendText) {
        var $fieldset = $('<fieldset>');
        if (legendText) {
            var $legend = $('<legend>', { class : 'section-legend' }).append(legendText)
            $fieldset.append($legend);
        }
        return $fieldset;
    }

    this.getLoader = function() {
        return $('<div>').attr('class', 'loader col-centered');
    }

    // blocks given $component or whole screen of none given
    this.startLoading = function($component) {
        var params = {
            message : this.getLoader(),
            css: {
                border: 'none',
                padding: '15px',
                backgroundColor: 'transparent',
            },
            overlayCSS: {
                opacity : 0.1,
            }
        };
        if ($component) {
            $component.block(params);
        } else {
            $.blockUI(params);
        }
    }

    // unblocks given $component or whole screen of none given
    this.stopLoading = function($component) {
        if ($component) {
            $component.unblock();
        } else {
            $.unblockUI();
        }
    }
}