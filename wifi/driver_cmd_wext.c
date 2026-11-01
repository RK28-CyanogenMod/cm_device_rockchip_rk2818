/*
 * Generic Android WEXT commands for the legacy RK28 WLAN driver.
 * Standard wireless ioctls only; no Broadcom private command ABI.
 * SPDX-License-Identifier: BSD-3-Clause
 */
#include "includes.h"
#include "common.h"
#include "driver.h"
#include "driver_wext.h"
#include "linux_wext.h"
#include "linux_ioctl.h"
#include "android_drv.h"

static void init_request(struct wpa_driver_wext_data *drv, struct iwreq *req)
{
    os_memset(req, 0, sizeof(*req));
    os_strlcpy(req->ifr_name, drv->ifname, IFNAMSIZ);
}

int wpa_driver_signal_poll(void *priv, struct wpa_signal_info *si)
{
    struct wpa_driver_wext_data *drv = priv;
    struct iwreq req;
    struct iw_statistics stats;
    os_memset(si, 0, sizeof(*si));
    os_memset(&stats, 0, sizeof(stats));
    init_request(drv, &req);
    req.u.data.pointer = &stats;
    req.u.data.length = sizeof(stats);
    req.u.data.flags = 1;
    if (ioctl(drv->ioctl_sock, SIOCGIWSTATS, &req) < 0)
        return -1;
    if (stats.qual.updated & IW_QUAL_LEVEL_INVALID)
        return -1;
    si->current_signal = stats.qual.level;
    if ((stats.qual.updated & IW_QUAL_DBM) || stats.qual.level > 127)
        si->current_signal = (signed char)stats.qual.level;
    init_request(drv, &req);
    if (ioctl(drv->ioctl_sock, SIOCGIWRATE, &req) == 0)
        si->current_txrate = req.u.bitrate.value / 1000;
    return 0;
}

int wpa_driver_wext_driver_cmd(void *priv, char *cmd, char *buf, size_t len)
{
    struct wpa_driver_wext_data *drv = priv;
    struct iwreq req;
    struct wpa_signal_info si;
    u8 mac[ETH_ALEN];
    int ret;
    if (os_strcasecmp(cmd, "START") == 0 ||
            os_strcasecmp(cmd, "STOP") == 0) {
        int up = os_strcasecmp(cmd, "START") == 0;
        ret = linux_set_iface_flags(drv->ioctl_sock, drv->ifname, up);
        if (ret == 0) {
            drv->driver_is_started = up;
            wpa_msg(drv->ctx, MSG_INFO, WPA_EVENT_DRIVER_STATE "%s",
                    up ? "STARTED" : "STOPPED");
        }
        return ret;
    }
    if (os_strcasecmp(cmd, "MACADDR") == 0) {
        if (linux_get_ifhwaddr(drv->ioctl_sock, drv->ifname, mac) < 0)
            return -1;
        return os_snprintf(buf, len, "Macaddr = " MACSTR, MAC2STR(mac));
    }
    if (os_strcasecmp(cmd, "RSSI") == 0 ||
            os_strcasecmp(cmd, "RSSI-APPROX") == 0) {
        if (wpa_driver_signal_poll(priv, &si) < 0) return -1;
        return os_snprintf(buf, len, "rssi %d", si.current_signal);
    }
    if (os_strcasecmp(cmd, "LINKSPEED") == 0) {
        init_request(drv, &req);
        if (ioctl(drv->ioctl_sock, SIOCGIWRATE, &req) < 0) return -1;
        return os_snprintf(buf, len, "LinkSpeed %d",
                req.u.bitrate.value / 1000000);
    }
    if (os_strncasecmp(cmd, "POWERMODE ", 10) == 0) {
        init_request(drv, &req);
        req.u.power.disabled = atoi(cmd + 10) != 0;
        req.u.power.flags = IW_POWER_ON;
        return ioctl(drv->ioctl_sock, SIOCSIWPOWER, &req);
    }
    if (os_strcasecmp(cmd, "GETPOWER") == 0) {
        init_request(drv, &req);
        if (ioctl(drv->ioctl_sock, SIOCGIWPOWER, &req) < 0) return -1;
        return os_snprintf(buf, len, "powermode = %d", req.u.power.disabled);
    }
    /* Optional Android policies have no equivalent in this old driver.
     * Ordinary WEXT scanning/association remains active. */
    if (os_strncasecmp(cmd, "RXFILTER-", 9) == 0 ||
            os_strncasecmp(cmd, "BTCOEX", 6) == 0 ||
            os_strncasecmp(cmd, "SETSUSPEND", 10) == 0 ||
            os_strcasecmp(cmd, "SCAN-ACTIVE") == 0 ||
            os_strcasecmp(cmd, "SCAN-PASSIVE") == 0)
        return 0;
    wpa_printf(MSG_DEBUG, "RK28 WEXT: unsupported Android command: %s", cmd);
    return -1;
}
