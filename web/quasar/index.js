import Quasar, {
  // plugins
  Dialog,
  Loading,
  Notify,
  Cookies,
  Dark,
  // components
  QLayout,
  QPage,
  QPageContainer,
  QHeader,
  QFooter,
  QToolbar,
  QToolbarTitle,
  QBanner,
  QDialog,
  QAjaxBar,
  QAvatar,
  QIcon,
  QTooltip,
  QSpace,
  QSeparator,
  // Button
  QBtn,
  QBtnDropdown,
  // form
  QForm,
  QInput,
  QSelect,
  // card
  QCard,
  QCardActions,
  QCardSection,
  // item list
  QList,
  QItem,
  QItemLabel,
  QItemSection,
  // 
  QVirtualScroll,
  // table
  QTable,
  QTh,
  QTr,
  QTd,
  // tab
  QTabs,
  QRouteTab,
  QPopupProxy,
  ClosePopup
} from "quasar";
import langZhHans from "quasar/lang/zh-hans";
import iconSet from "quasar/icon-set/mdi-v3";
import "@quasar/extras/mdi-v3/mdi-v3.css";
import "./quasar-variables.styl";
import Vue from "vue";

Vue.use(Quasar, {
  lang: langZhHans,
  iconSet,
  components: {
    QLayout,
    QPage,
    QPageContainer,
    QHeader,
    QFooter,
    QToolbar,
    QToolbarTitle,
    QBanner,
    QDialog,
    QAjaxBar,
    QAvatar,
    QIcon,
    QTooltip,
    QSpace,
    QSeparator,
    QBtn,
    QBtnDropdown,
    QForm,
    QInput,
    QSelect,
    QCard,
    QCardActions,
    QCardSection,
    QList,
    QItem,
    QItemLabel,
    QItemSection,
    QVirtualScroll,
    QTable,
    QTh,
    QTr,
    QTd,
    QTabs,
    QRouteTab,
    QPopupProxy
  },
  directives: {
    ClosePopup
  },
  plugins: {
    Loading,
    Dialog,
    Notify,
    Cookies,
    Dark
  }
});
