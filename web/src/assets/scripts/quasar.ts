import Quasar, {
  // components
  Dialog,
  Loading,
  Notify,
  QAjaxBar,
  QAvatar,
  QBtn,
  QCard,
  QCardActions,
  QCardSection,
  QDrawer,
  QFooter,
  QForm,
  QHeader,
  QIcon,
  QInput,
  QItem,
  // form
  QItemLabel,
  QItemSection,
  // card
  QLayout,
  QList,
  QPage,
  // plugins
  QPageContainer,
  QToolbar,
  QToolbarTitle,
} from 'quasar'
import iconSet from 'quasar/icon-set/fontawesome-v5.js'
import Vue from 'vue'

Vue.use(Quasar as any, {
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
  },
  plugins: {
    Loading,
    Dialog,
    Notify,
  },
  iconSet,
})
