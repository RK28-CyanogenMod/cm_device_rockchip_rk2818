/*
 * RK2818 PV common/format libraries retain a DT_NEEDED entry for the removed
 * SurfaceFlinger client library, without importing any of its symbols.
 * Keep that dependency loadable; no legacy Surface API is implemented here.
 */
