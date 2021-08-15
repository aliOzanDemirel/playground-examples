import Loadable from 'react-loadable';
import * as React from "react";

const Loading = (props) => {
    if (props.error) {
        return <div>Could not load component!</div>;
    } else if (props.pastDelay) {
        return <div>Loading...</div>;
    } else {
        return null;
    }
};

// default delay of Loadable component is 200ms
export const dashboardItemRoutes = [{
    path: '/admin/dashboard/report-mailing',
    component: Loadable({
        loader: () => import('../components/DashboardContents/MailAndDownloadReport/MailAndDownloadReport'),
        loading: Loading
    })
}, {
    path: '/admin/dashboard/sap-options',
    component: Loadable({
        loader: () => import('../components/DashboardContents/SapManagement/SapManagement'),
        loading: Loading
    })
}, {
    path: '/admin/dashboard/users',
    component: Loadable({
        loader: () => import('../components/DashboardContents/UserManagement/UserManagement'),
        loading: Loading
    })
}, {
    path: '/admin/dashboard/content-repo',
    component: Loadable({
        loader: () => import('../components/DashboardContents/ContentManagement/ContentManagement'),
        loading: Loading
    })
}, {
    path: '/admin/dashboard/search-error',
    component: Loadable({
        loader: () => import('../components/DashboardContents/ErrorManagement/ErrorSearchManagement'),
        loading: Loading
    })
}, {
    path: '/admin/dashboard/cron-jobs',
    component: Loadable({
        loader: () => import('../components/DashboardContents/CronJobsManagement/CronJobsManagement'),
        loading: Loading
    })
}, {
    path: '/admin/dashboard/manage-login',
    component: Loadable({
        loader: () => import('../components/DashboardContents/LoginManagement/LoginManagement'),
        loading: Loading
    })
}, {
    path: '/admin/dashboard/indexing',
    component: Loadable({
        loader: () => import('../components/DashboardContents/IndexingManagement/IndexingManagement'),
        loading: Loading
    })
}, {
    path: '/admin/dashboard/code-editor',
    component: Loadable({
        loader: () => import('../components/DashboardContents/CodeEditor/CodeEditor'),
        loading: Loading
    })
}, {
    path: '/admin/dashboard/config-uploader',
    component: Loadable({
        loader: () => import('../components/DashboardContents/ConfigFileManagement/ConfigFileManagement'),
        loading: Loading
    })
}, {
    path: '/admin/dashboard/global-admin',
    component: Loadable({
        loader: () => import('../components/DashboardContents/SwitchStatusManagement/SwitchStatusManagement'),
        loading: Loading
    })
}, {
    path: '/admin/dashboard/sites',
    component: Loadable({
        loader: () => import('../components/DashboardContents/SiteManagement/SiteManagement'),
        loading: Loading
    })
}, {
    path: '/admin/dashboard/caches',
    component: Loadable({
        loader: () => import('../components/DashboardContents/CacheManagement/CacheManagement'),
        loading: Loading
    })
}];