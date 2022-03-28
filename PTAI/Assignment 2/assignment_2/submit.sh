#!/bin/sh

#SBATCH --time=0:10:00
#SBATCH --nodes=1
#SBATCH -A ngcom018c # nuig02 is NUI Galway condominium access; a Class-C project would have a differe value here
#SBATCH -p DevQ
#SBATCH --mail-user=M.AguilarGarcia1@nuigalway.ie
#SBATCH --mail-type=BEGIN,END

module load taskfarm
module load conda/2
source activate myenv # we have to have created a conda environment with this name: see docs

cd $SLURM_SUBMIT_DIR
taskfarm taskfarm.sh # we have to supply the file taskfarm.sh: see docs
