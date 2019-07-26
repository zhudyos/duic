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
  // plugins
  QPage,
  QPageContainer,
  QSpace,
  QToolbar,
  QToolbarTitle,
} from 'quasar'
import iconSet from 'quasar/icon-set/material-icons.js'
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
  },
  plugins: {
    Loading,
    Dialog,
    Notify,
  },
})
