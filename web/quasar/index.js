import Quasar, {
  // components
  Dialog,
  Loading,
  Notify,
  QAjaxBar,
  QAvatar,
  QBtn,
  QBtnToggle,
  QCard,
  QCardActions,
  QCardSection,
  QDrawer,
  QFooter,
  QForm,
  QHeader,
  QIcon,
  QInput,
  // form
  QItem,
  QItemLabel,
  // card
  QItemSection,
  QLayout,
  QList,
  // table
  QTable,
  QTh,
  QTr,
  QTd,
  // tab
  QTabs,
  QRouteTab,
  // plugins
  QPage,
  QPageContainer,
  QSpace,
  QToolbar,
  QToolbarTitle,
} from 'quasar'
import iconSet from 'quasar/icon-set/fontawesome-v5.js'
import '@quasar/extras/fontawesome-v5/fontawesome-v5.css'
import './quasar-variables.styl'
import Vue from 'vue'

Vue.use(Quasar, {
  iconSet,
  components: {
    QAjaxBar,
    QLayout,
    QPageContainer,
    QPage,
    QHeader,
    QFooter,
    QDrawer,
    QAvatar,
    QToolbar,
    QToolbarTitle,
    QBtn,
    QIcon,
    QList,
    QItem,
    QItemLabel,
    QItemSection,
    QForm,
    QInput,
    QCard,
    QCardSection,
    QCardActions,
    QBtnToggle,
    QSpace,
    QTable,
    QTh,
    QTr,
    QTd,
    QTabs,
    QRouteTab,
  },
  plugins: {
    Loading,
    Dialog,
    Notify,
  },
})
