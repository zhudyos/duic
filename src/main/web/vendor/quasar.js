import Vue from "vue"
import Quasar, {
    // components
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
    // form
    QForm,
    QInput,
    // card
    QCard,
    QCardSection,
    QCardActions,
    // plugins
    Loading,
    Dialog,
    Notify
} from "quasar"

import iconSet from "quasar/icon-set/fontawesome-v5.js"
import "@quasar/extras/fontawesome-v5/fontawesome-v5.css"

Vue.use(Quasar, {
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
        QCardActions
    },
    plugins: {
        Loading,
        Dialog,
        Notify
    },
    iconSet: iconSet
})