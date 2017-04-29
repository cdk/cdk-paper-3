
all: chembl_22_1 chebi_149

chembl_22_1: chembl_22_1.smi chembl_22_1.sdf	

chebi_149: chebi_149.smi chebi_149.sdf

chembl_22_1.smi:
	LC_ALL=C curl ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBLdb/releases/chembl_22_1/chembl_22_1_chemreps.txt.gz | gunzip -c | grep -vF 'chembl_id' | awk '{print $$2 " " $$1}' > chembl_22_1.smi

chebi_149.sdf:	
	curl ftp://ftp.ebi.ac.uk/pub/databases/chebi/archive/rel149/SDF/ChEBI_complete_3star.sdf.gz | gunzip -c > chebi_149.sdf

chebi_149.smi: chebi_149.sdf
	LC_ALL=C perl get_chebi_smi.pl chebi_149.sdf > chebi_149.smi

chembl_22_1.sdf:
	LC_ALL=C curl ftp://ftp.ebi.ac.uk/pub/databases/chembl/ChEMBLdb/releases/chembl_22_1/chembl_22_1.sdf.gz | gunzip -c > chembl_22_1.sdf
