-- Execute este script UMA VEZ no Oracle SQL Developer.
-- As colunas são opcionais (NULL) — registros existentes não são afetados.

-- Coordenadas para o heatmap do admin
ALTER TABLE T_SN_PACIENTE ADD (LATITUDE NUMBER, LONGITUDE NUMBER);
ALTER TABLE T_SN_DENTISTA ADD (LATITUDE NUMBER, LONGITUDE NUMBER);

-- Estado para geocoding preciso via Nominatim
ALTER TABLE T_SN_PACIENTE ADD (ESTADO VARCHAR2(100));
ALTER TABLE T_SN_DENTISTA ADD (ESTADO VARCHAR2(100));
