;; This Source Code Form is subject to the terms of the Mozilla Public
;; License, v. 2.0. If a copy of the MPL was not distributed with this
;; file, You can obtain one at http://mozilla.org/MPL/2.0/.
;;
;; This Source Code Form is "Incompatible With Secondary Licenses", as
;; defined by the Mozilla Public License, v. 2.0.
;;
;; Copyright (c) 2020 UXBOX Labs SL

(ns app.main.ui.workspace.sidebar.options.component
  (:require
   [rumext.alpha :as mf]
   [app.common.pages-helpers :as cph]
   [app.main.refs :as refs]
   [app.main.store :as st]
   [app.main.ui.icons :as i]
   [app.main.ui.components.context-menu :refer [context-menu]]
   [app.main.data.workspace.common :as dwc]
   [app.main.data.workspace.libraries :as dwl]
   [app.util.i18n :as i18n :refer [t]]
   [app.util.dom :as dom]))

(def component-attrs [:component-id :component-file :shape-ref])

(mf/defc component-menu
  [{:keys [ids values] :as props}]
  (let [id     (first ids)
        locale (mf/deref i18n/locale)
        local  (mf/use-state {:menu-open false})

        show?         (some? (:component-id values))
        local-library (mf/deref refs/workspace-local-library)
        libraries     (mf/deref refs/workspace-libraries)
        component     (cph/get-component (:component-id values)
                                         (:component-file values)
                                         local-library
                                         libraries)

        on-menu-click (mf/use-callback
                        (fn [event]
                          (dom/prevent-default event)
                          (dom/stop-propagation event)
                          (swap! local assoc :menu-open true)))

        on-menu-close (mf/use-callback
                        #(swap! local assoc :menu-open false))

        do-detach-component #(st/emit! (dwl/detach-component id))
        do-reset-component #(st/emit! (dwl/reset-component id))
        do-update-component #(do
                               (st/emit! dwc/start-undo-transaction)
                               (st/emit! (dwl/update-component id))
                               (st/emit! (dwl/sync-file nil))
                               (st/emit! dwc/commit-undo-transaction))
        do-navigate-component-file #(st/emit! (dwl/nav-to-component-file
                                                (:component-file values)))]
    (when show?
      [:div.element-set
       [:div.element-set-title
        [:span (t locale "workspace.options.component")]]
       [:div.element-set-content
        [:div.row-flex.component-row
         i/component
         (:name component)
         [:div.row-actions
          {:on-click on-menu-click}
          i/actions
          [:& context-menu {:on-close on-menu-close
                            :show (:menu-open @local)
                            :options [[(t locale "workspace.shape.menu.detach-instance") do-detach-component]
                                      [(t locale "workspace.shape.menu.reset-overrides") do-reset-component]
                                      (if (:component-file values)
                                        [(t locale "workspace.shape.menu.go-master") do-navigate-component-file]
                                        [(t locale "workspace.shape.menu.update-master") do-update-component])]
         }]]]]])))

