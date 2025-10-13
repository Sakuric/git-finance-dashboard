'use strict';

Object.defineProperty(exports, '__esModule', { value: true });

var iconsVue = require('@element-plus/icons-vue');
var button = require('../../button/src/button.js');
var runtime = require('../../../utils/vue/props/runtime.js');
var icon = require('../../../utils/vue/icon.js');
var content = require('../../tooltip/src/content.js');

const popconfirmProps = runtime.buildProps({
  title: String,
  confirmButtonText: String,
  cancelButtonText: String,
  confirmButtonType: {
    type: String,
    values: button.buttonTypes,
    default: "primary"
  },
  cancelButtonType: {
    type: String,
    values: button.buttonTypes,
    default: "text"
  },
  icon: {
    type: icon.iconPropType,
    default: () => iconsVue.QuestionFilled
  },
  iconColor: {
    type: String,
    default: "#f90"
  },
  hideIcon: Boolean,
  hideAfter: {
    type: Number,
    default: 200
  },
  effect: {
    ...content.useTooltipContentProps.effect,
    default: "light"
  },
  teleported: content.useTooltipContentProps.teleported,
  persistent: content.useTooltipContentProps.persistent,
  width: {
    type: [String, Number],
    default: 150
  },
  closeOnPressEscape: {
    type: Boolean,
    default: true
  }
});
const popconfirmEmits = {
  confirm: (e) => e instanceof MouseEvent,
  cancel: (e) => e instanceof MouseEvent || e instanceof KeyboardEvent
};

exports.popconfirmEmits = popconfirmEmits;
exports.popconfirmProps = popconfirmProps;
//# sourceMappingURL=popconfirm.js.map
